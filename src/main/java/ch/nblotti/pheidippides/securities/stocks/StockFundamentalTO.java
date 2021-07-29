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
public abstract  class StockFundamentalTO {


    @Id
    private Integer id;

    @Column(name = "YEAR")
    int year;

    @Column(name = "EXCHANGE")
    String exchangeShortName;

    @Column(name = "CODE")
    String code;

    @Column(name = "ISIN")
    private String isin;

    @Column(name = "book_value")
    public double bookValue;

    @Column(name = "ebitda")
    public long ebitda;

    @Column(name = "pe_ratio")
    public double peratio;

    @Column(name = "trailing_pe")
    public float trailingPE;

    @Column(name = "dividend_share")
    public double dividendShare;

    @Column(name = "dividend_yield")
    public double dividendYield;

    @Column(name = "earning_share")
    public double earningsShare;

    @Column(name = "profit_margin")
    public double profitMargin;

    @Column(name = "operting_margin_ttm")
    public double operatingMarginTTM;

    @Column(name = "return_on_assets_ttm")
    public double returnOnAssetsTTM;

    @Column(name = "return_on_equity_ttm")
    public double returnOnEquityTTM;

    @Column(name = "revenue_ttm")
    public long revenueTTM;

    @Column(name = "price_sales_ttm")
    public float priceSalesTTM;

    @Column(name = "revenue_per_share_ttm")
    public double revenuePerShareTTM;

    @Column(name = "gross_profit_ttm")
    public long grossProfitTTM;

    @Column(name = "diluted_eps_ttm")
    public double dilutedEpsTTM;


}
