package ch.nblotti.pheidippides.kafka.container;

import ch.nblotti.pheidippides.kafka.quote.QuoteKeyWrapper;
import ch.nblotti.pheidippides.kafka.quote.QuoteWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class Container implements Serializable {
    QuoteKeyWrapper quoteKeyWrapper;
    QuoteWrapper quoteWrapper;
}