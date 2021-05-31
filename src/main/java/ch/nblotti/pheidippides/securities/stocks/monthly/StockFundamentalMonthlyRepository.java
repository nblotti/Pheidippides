package ch.nblotti.pheidippides.securities.stocks.monthly;

import ch.nblotti.pheidippides.securities.stocks.StockFundamentalRepository;

import javax.transaction.Transactional;

@Transactional
public  interface StockFundamentalMonthlyRepository extends StockFundamentalRepository<StockFundamentalMonthlyTO> {
}
