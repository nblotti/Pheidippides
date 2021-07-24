package ch.nblotti.pheidippides.kafka.quote;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class QuoteSerdes implements Serde<QuoteWrapper> {
    @Override
    public Serializer<QuoteWrapper> serializer() {
        return new QuoteSerializer();
    }

    @Override
    public Deserializer<QuoteWrapper> deserializer() {
        return new QuoteDeserializer();
    }
}
