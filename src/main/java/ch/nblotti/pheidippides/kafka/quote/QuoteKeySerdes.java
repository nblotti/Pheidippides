package ch.nblotti.pheidippides.kafka.quote;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class QuoteKeySerdes implements Serde<QuoteKeyWrapper> {
    @Override
    public Serializer<QuoteKeyWrapper> serializer() {
        return new QuoteKeySerializer();
    }

    @Override
    public Deserializer<QuoteKeyWrapper> deserializer() {
        return new QuoteKeyDeserializer();
    }
}
