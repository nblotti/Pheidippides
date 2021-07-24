package ch.nblotti.pheidippides.kafka.container;

import ch.nblotti.pheidippides.kafka.quote.QuoteWrapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class ContainerSerdes implements Serde<Container> {
    @Override
    public Serializer<Container> serializer() {
        return new ContainerSerializer();
    }

    @Override
    public Deserializer<Container> deserializer() {
        return new ContainerDeserializer();
    }
}
