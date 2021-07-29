package ch.nblotti.pheidippides.securities.etfs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class EtfTo {

    @Id
    private Integer id;

    @Column(name = "EXCHANGE")
    String exchangeShortName;

    @Column(name = "CODE")
    String code;

    @Column(name = "YEAR")
    int year;

    @Column(name = "ISIN")
    private String isin;

    @Column(name = "CURRENCY_CODE")
    private String currencyCode;

    @Column(name = "COUNTRY_ISO")
    private String countryISO;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "YIELD")
    private double etfYield;

    @Column(name = "NET_EXPENSE_RATIO")
    private double netExpenseRatio;

    @Column(name = "TOTAL_ASSETS")
    private double totalAssets;

    @Column(name = "AVERAGE_MKT_CAP_MIL")
    private double averageMktCapMil;

    @Column(name = "BASIC_MATERIALS")
    private double basicMaterials;

    @Column(name = "CONSUMER_CYCLICALS")
    private double consumerCyclicals;

    @Column(name = "FINANCIAL_SERVICES")
    private double financialServices;

    @Column(name = "REAL_ESTATE")
    private double realEstate;

    @Column(name = "COMMUNICATION_SERVICES")
    private double communicationServices;

    @Column(name = "ENERGY")
    private double energy;

    @Column(name = "INDUSTRIALS")
    private double industrials;

    @Column(name = "TECHNOLOGY")
    private double technology;

    @Column(name = "CONSUMER_DEFENSIVE")
    private double consumerDefensive;

    @Column(name = "HEALTHCARE")
    private double healthcare;

    @Column(name = "UTILITIES")
    private double utilities;


}
