package ch.nblotti.pheidippides.kafka.quote;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Quote {

    private String exchange;
    private String code;
    private SQL_OPERATION operation;

}
