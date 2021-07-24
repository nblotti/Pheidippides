package ch.nblotti.pheidippides.kafka.quote;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

@Slf4j
public class QuoteKeyDeserializer implements Deserializer<QuoteKeyWrapper> {


    @Override
    public QuoteKeyWrapper deserialize(String s, byte[] in) {

        QuoteKeyWrapper quoteKeyWrapper = new QuoteKeyWrapper();
        quoteKeyWrapper.setIn(in);
        return quoteKeyWrapper;
    }
}
