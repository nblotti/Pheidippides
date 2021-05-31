package ch.nblotti.pheidippides.securities.stocks.monthly;

import ch.nblotti.pheidippides.securities.stocks.StockFundamentalTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "STOCK_FUNDAMENTAL_MONTHLY")
public class StockFundamentalMonthlyTO extends StockFundamentalTO {

    @Column(name = "MONTH_NUMBER")
    int month_number;




}
