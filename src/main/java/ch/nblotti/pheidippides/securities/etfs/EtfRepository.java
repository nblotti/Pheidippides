package ch.nblotti.pheidippides.securities.etfs;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public abstract interface EtfRepository<T extends EtfTo> extends PagingAndSortingRepository<T, Long> {

}
