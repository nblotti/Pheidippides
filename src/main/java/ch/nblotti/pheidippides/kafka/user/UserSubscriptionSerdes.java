package ch.nblotti.pheidippides.kafka.user;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class UserSubscriptionSerdes implements Serde<UserSubscription> {
    @Override
    public Serializer<UserSubscription> serializer() {
        return new UserSubscriptionSerializer();
    }

    @Override
    public Deserializer<UserSubscription> deserializer() {
        return new UserSubscriptionDeserializer();
    }
}
