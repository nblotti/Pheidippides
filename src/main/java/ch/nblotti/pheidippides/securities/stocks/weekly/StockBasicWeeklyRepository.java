package ch.nblotti.pheidippides.securities.stocks.weekly;

import ch.nblotti.pheidippides.securities.stocks.StockBasicRepository;

import javax.transaction.Transactional;

@Transactional
public interface StockBasicWeeklyRepository extends StockBasicRepository<StockBasicWeeklyTO> {
}
