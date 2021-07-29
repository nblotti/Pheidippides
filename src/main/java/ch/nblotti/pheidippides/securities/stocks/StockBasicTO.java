package ch.nblotti.pheidippides.securities.stocks;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class StockBasicTO {


    @Id
    private Integer id;

    @Column(name = "EXCHANGE")
    String exchangeShortName;

    @Column(name = "CODE")
    String code;

    @Column(name = "ISIN")
    private String isin;

    @Column(name = "YEAR")
    int year;

    @Column(name = "COUNTRY_ISO")
    private String countryISO;

    @Column(name = "GIC_SECTOR")
    String gicSector;

    @Column(name = "GIC_GROUP")
    String gicGroup;

    @Column(name = "GIC_INDUSTRY")
    String gicIndustry;

    @Column(name = "GIC_SUB_INDUSTRY")
    String gicSubIndustry;


    @Column(name = "MEDIAN_MARKET_CAP")
    long medianMarketCapitalization;

    @Column(name = "MEDIAN_ADJUSTED_CLOSE")
    float medianAdjustedClose;

    @Column(name = "MEDIAN_VOLUME")
    long medianVolume;

    @Column(name = "AVG_MARKET_CAP")
    long averageMarketCapitalization;

    @Column(name = "AVG_ADJUSTED_CLOSE")
    float averageAdjustedClose;

    @Column(name = "AVG_VOLUME")
    long averageVolume;

    @Column(name = "shares_outstanding")
    public long sharesOutstanding;

    @Column(name = "shares_float")
    public long sharesFloat;

    @Column(name = "percent_insiders")
    public float percentInsiders;

    @Column(name = "percent_institutions")
    public float percentInstitutions;

    @Column(name = "shares_short")
    public long sharesShort;

    @Column(name = "short_ratio")
    public float shortRatio;

    @Column(name = "short_percent_outstanding")
    public float shortPercentOutstanding;

    @Column(name = "short_percent_float")
    public float shortPercentFloat;


}
