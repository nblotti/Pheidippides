package ch.nblotti.pheidippides.securities.stocks.weekly;

import ch.nblotti.pheidippides.securities.stocks.StockBasicTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "STOCK_BASIC_WEEKLY")
public class StockBasicWeeklyTO extends StockBasicTO {
    @Column(name = "WEEK_NUMBER")
    int weekNumber;
}
