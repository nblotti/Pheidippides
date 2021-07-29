package ch.nblotti.pheidippides.kafka.quote;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class QuoteKeyWrapper implements Serializable {

    private byte[] in;


}
