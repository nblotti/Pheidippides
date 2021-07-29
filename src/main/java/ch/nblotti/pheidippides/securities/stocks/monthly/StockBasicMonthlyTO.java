package ch.nblotti.pheidippides.securities.stocks.monthly;

import ch.nblotti.pheidippides.securities.stocks.StockBasicTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "STOCK_BASIC_MONTHLY")
public class StockBasicMonthlyTO extends StockBasicTO {

    @Column(name = "MONTH_NUMBER")
    int monthNumber;
}
