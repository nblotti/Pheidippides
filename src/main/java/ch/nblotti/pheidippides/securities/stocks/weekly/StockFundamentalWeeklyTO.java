package ch.nblotti.pheidippides.securities.stocks.weekly;

import ch.nblotti.pheidippides.securities.stocks.StockFundamentalTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "STOCK_FUNDAMENTAL_WEEKLY")
public class StockFundamentalWeeklyTO extends StockFundamentalTO {

    @Column(name = "WEEK_NUMBER")
    int week_number;

}
