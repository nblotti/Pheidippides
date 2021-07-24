package ch.nblotti.pheidippides.kafka.container;

import ch.nblotti.pheidippides.kafka.quote.QuoteWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class ContainerSerializer implements Serializer<Container> {


    @Override
    public byte[] serialize(String s, Container container) {

        return SerializationUtils.serialize(container);
    }
}
