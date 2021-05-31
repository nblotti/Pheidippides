package ch.nblotti.pheidippides.securities.stocks.monthly;

import ch.nblotti.pheidippides.securities.stocks.StockBasicRepository;

import javax.transaction.Transactional;

@Transactional
public interface StockBasicMonthlyRepository extends StockBasicRepository<StockBasicMonthlyTO> {
}
