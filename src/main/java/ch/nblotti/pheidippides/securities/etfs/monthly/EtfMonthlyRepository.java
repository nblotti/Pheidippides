package ch.nblotti.pheidippides.securities.etfs.monthly;

import ch.nblotti.pheidippides.securities.etfs.EtfRepository;

import javax.transaction.Transactional;

@Transactional
public interface EtfMonthlyRepository extends EtfRepository<EtfMonthlyTo> {
}
