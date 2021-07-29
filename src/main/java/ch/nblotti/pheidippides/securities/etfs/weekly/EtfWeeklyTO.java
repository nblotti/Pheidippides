package ch.nblotti.pheidippides.securities.etfs.weekly;

import ch.nblotti.pheidippides.securities.etfs.EtfTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ETF_WEEKLY")
public class EtfWeeklyTO extends EtfTO {
    @Column(name = "WEEK_NUMBER")
    int weekNumber;
}
