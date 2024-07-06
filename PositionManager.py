from datetime import datetime as dt, datetime

import numpy as np
import pandas as pd

import config
from Utils import timing
from eod.EODRepository import get_eod_stock_data


class PositionManager:

    def __init__(self):

        # utilisé dans pour le calcul de la quantité, du cma et de la performance pour conserver le row prédécent
        # lors de l'itéaration sur le dataframe des positions. Voir get_ordered_quantity si dessous
        self.prev_row = {}

    # -----------------------------------------------------------------------------------------------------------------#
    # 'orderEffectiveDateLookup' # tickerSymbol # placeOfTrade # netQuantity # orderedAmountCurrency # netPositionAmount
    # cmaNet # returnTwr #
    #

    @timing
    def get_positions(self, pd_entries, start_date=None, aggregate_positions=None):
        ###############################################################################################################

        # 1. On prépare les moouverments pour les mettre au format qui permetttont la jointure
        pd_entries['orderEffectiveDateLookup'] = pd.to_datetime(pd_entries['orderEffectiveDate']).dt.strftime(
            "%Y-%m-%d 00:00:00")
        pd_entries['orderEffectiveDateLookup'] = pd.to_datetime(pd_entries['orderEffectiveDateLookup'])
        pd_entries['orderEffectiveDateIndex'] = pd.to_datetime(pd_entries['orderEffectiveDate'])
        pd_entries['orderedQuantity'] = pd.to_numeric(pd_entries['orderedQuantity'])
        pd_entries = pd_entries.set_index(['orderEffectiveDateIndex'])
        # pd_entries.sort_index(inplace=True)
        ###############################################################################################################
        # 2. On crée une dataframe position, avec un row par date entre la date du premier mouvement et la date
        # transmise. Dans le cas ou on a pas reçu de date, on utilise celle du premier  mouvement trouvé
        if start_date is None:
            start_date = pd_entries['orderEffectiveDate'].iloc[0]
            datetime.strptime(start_date, "%Y-%m-%dT%H:%M:%S")
        else:
            start_date = datetime.strptime(start_date, config.data_format_str)
        end_date = dt.now()
        range = pd.bdate_range(start=start_date, end=end_date)

        # on crée une structure contenant chacune des dates entre la première entrée et la date courante
        business_days = pd.DataFrame(range.strftime(config.data_format_str),
                                     columns=['orderEffectiveDateLookup'], index=range)

        business_days['orderEffectiveDateLookup'] = pd.to_datetime(business_days['orderEffectiveDateLookup'],
                                                                   format=config.data_format_str)

        ###############################################################################################################
        # 3.3. On aggrége dans le cas ou il existe plusieurs mouvements sur une même date et on merge les positions et les
        # mouvements

        df_positions: pd.DataFrame
        if aggregate_positions:
            df_positions = (pd_entries.drop(columns=['orderEffectiveDate']).groupby(
                ['accountId', 'tickerSymbol', 'orderEffectiveDateLookup', 'orderedAmountCurrency', 'placeOfTrade'])
            .agg({'orderedQuantity': 'sum', 'orderedAmountPrice': 'sum'}).sort_values(
                'orderEffectiveDateLookup')).reset_index()

        else:
            df_positions = pd_entries.drop(columns=['orderEffectiveDate'])

        positions = pd.merge(business_days, df_positions, left_on='orderEffectiveDateLookup',
                             right_on='orderEffectiveDateLookup', how="left", sort=True)
        ###############################################################################################################
        # 4. On complète la nouvelle structure en ajoutant les champs nécessaires et en les complétant si possible
        # on met une valeur par default pour les autres

        positions.rename(columns={'orderEffectiveDateLookup': 'positionDateLookup'}, inplace=True)
        positions["orderedAmountCurrency"] = pd_entries.iloc[0]["orderedAmountCurrency"]
        positions.fillna({"orderedAmountCurrency": 0}, inplace=True)
        positions["accountId"] = pd_entries.iloc[0]["accountId"]
        positions["tickerSymbol"] = pd_entries.iloc[0]["tickerSymbol"]
        positions["placeOfTrade"] = pd_entries.iloc[0]["placeOfTrade"]
        positions["netQuantity"] = 0
        positions["cmaNet"] = 0
        positions["netPositionAmount"] = 0
        positions["deleteMe"] = 0
        positions.fillna({"orderedQuantity": 0, "orderedAmountPrice": 0}, inplace=True)

        ###############################################################################################################
        # 4. On ajoute les prix du marché
        prices = get_eod_stock_data(pd_entries.iloc[0]["tickerSymbol"], pd_entries.iloc[0]["placeOfTrade"])
        prices['date'] = pd.to_datetime(prices['date']).dt.strftime("%Y-%m-%d 00:00:00")
        prices['date'] = pd.to_datetime(prices['date'])

        positions = pd.merge(positions, prices, left_on="positionDateLookup", right_on="date", how="left", sort=True)
        positions.rename(columns={'adjusted_close': 'price'}, inplace=True)
        positions.drop(columns=['volume', 'date'], inplace=True)
        positions.dropna(subset=['price'], inplace=True)

        positions["netQuantity"] = np.cumsum(positions["orderedQuantity"])

        positions["netPositionAmount"] = np.multiply(positions["netQuantity"], positions["price"])
        yesterday = positions.shift(1)

        positions["netPositionAmount_without_entries"] = np.multiply(yesterday["netQuantity"], positions["price"])
        # calcul de la twr
        positions["return_twr"] = np.divide(
            np.subtract(positions["netPositionAmount_without_entries"], yesterday["netPositionAmount"], ),
            yesterday["netPositionAmount"])
        # calcul du cma =
        positions["transactionDone"] = np.cumsum(np.multiply(positions["orderedQuantity"], positions["price"]))
        positions["cmaNet"] = np.divide(positions["transactionDone"], positions["netQuantity"])

        positions["std"] = positions["price"].expanding().std(ddof=0)

        ###############################################################################################################
        # 4. On supprimer les positions non valorisée, on fait du cleanup et on retourne le résultat
        positions = positions[positions.deleteMe != True]

        positions.drop(columns=['deleteMe', 'netPositionAmount_without_entries', 'transactionDone'], inplace=True)

        return positions
