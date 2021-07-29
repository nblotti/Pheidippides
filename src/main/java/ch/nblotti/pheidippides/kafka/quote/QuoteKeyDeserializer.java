package ch.nblotti.pheidippides.kafka.quote;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class QuoteKeyDeserializer implements Deserializer<QuoteKeyWrapper> {


    @Override
    public QuoteKeyWrapper deserialize(String s, byte[] in) {

        QuoteKeyWrapper quoteKeyWrapper = new QuoteKeyWrapper();
        quoteKeyWrapper.setIn(in);
        return quoteKeyWrapper;
    }
}
