package ch.nblotti.pheidippides.kafka.quote;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class QuoteWrapper implements Serializable {

    private byte[] in;
    private String code;
    private SQL_OPERATION operation;


}
