package ch.nblotti.pheidippides.kafka.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

@Slf4j
public class UserSubscriptionSerializer implements Serializer<UserSubscription> {

    private final ModelMapper modelMapper = new ModelMapper();


    public UserSubscriptionSerializer() {
        initMapper();
    }

    private void initMapper() {
        Converter<UserSubscription, byte[]> userConverter = new AbstractConverter<UserSubscription, byte[]>() {

            @Override
            protected byte[] convert(UserSubscription userSubscription) {

                return null;
            }
        };
        modelMapper.addConverter(userConverter);
    }

    @Override
    public byte[] serialize(String s, UserSubscription userSubscription) {
        return modelMapper.map(userSubscription, byte[].class);
    }
}
