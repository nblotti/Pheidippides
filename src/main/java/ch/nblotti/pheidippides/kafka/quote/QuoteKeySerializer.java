package ch.nblotti.pheidippides.kafka.quote;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class QuoteKeySerializer implements Serializer<QuoteKeyWrapper> {


    @Override
    public byte[] serialize(String s, QuoteKeyWrapper quoteKeyWrapper) {
        return quoteKeyWrapper.getIn();
    }
}
