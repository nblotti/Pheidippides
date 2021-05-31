package ch.nblotti.pheidippides.securities.etfs.weekly;

import ch.nblotti.pheidippides.securities.etfs.EtfRepository;

import javax.transaction.Transactional;

@Transactional
public interface EtfWeeklyRepository extends EtfRepository<EtfWeeklyTO> {
}
