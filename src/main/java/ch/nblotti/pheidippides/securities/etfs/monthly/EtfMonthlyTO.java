package ch.nblotti.pheidippides.securities.etfs.monthly;

import ch.nblotti.pheidippides.securities.etfs.EtfTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ETF_MONTHLY")
public class EtfMonthlyTO extends EtfTO {

    @Column(name = "MONTH_NUMBER")
    int month_number;
}
