package ch.nblotti.pheidippides.securities.stocks.weekly;

import ch.nblotti.pheidippides.securities.stocks.StockFundamentalRepository;

import javax.transaction.Transactional;

@Transactional
public  interface StockFundamentalWeeklyRepository extends StockFundamentalRepository<StockFundamentalWeeklyTO> {
}
