package ch.nblotti.pheidippides.securities.etfs.monthly;

import ch.nblotti.pheidippides.securities.etfs.EtfTo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ETF_MONTHLY")
public class EtfMonthlyTo extends EtfTo {

    @Column(name = "MONTH_NUMBER")
    int monthNumber;
}
