package ch.nblotti.pheidippides.securities.etfs.weekly;

import ch.nblotti.pheidippides.securities.etfs.EtfTo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ETF_WEEKLY")
public class EtfWeeklyTo extends EtfTo {
    @Column(name = "WEEK_NUMBER")
    int week_number;
}
