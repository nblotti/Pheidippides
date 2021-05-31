package ch.nblotti.pheidippides.securities.stocks;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public abstract interface StockFundamentalRepository<T extends StockFundamentalTO> extends PagingAndSortingRepository<T, Long> {

}
