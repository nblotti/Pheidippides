package ch.nblotti.pheidippides.kafka.quote;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class QuoteSerializer implements Serializer<QuoteWrapper> {


    @Override
    public byte[] serialize(String s, QuoteWrapper quoteWrapper) {
        return quoteWrapper.getIn();
    }
}
