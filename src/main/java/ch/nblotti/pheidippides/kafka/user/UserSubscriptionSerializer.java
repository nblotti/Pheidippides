package ch.nblotti.pheidippides.kafka.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
public class UserSubscriptionSerializer implements Serializer<UserSubscription> {

    ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public byte[] serialize(String s, UserSubscription userSubscription) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(userSubscription).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
