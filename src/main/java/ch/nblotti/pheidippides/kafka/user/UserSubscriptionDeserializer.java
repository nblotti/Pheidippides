package ch.nblotti.pheidippides.kafka.user;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@GeneratedExcludeJacocoTestCoverage
public class UserSubscriptionDeserializer implements Deserializer<UserSubscription> {


    ObjectMapper objectMapper = new ObjectMapper();


    public UserSubscriptionDeserializer(){
        objectMapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));

    }
    @Override
    public UserSubscription deserialize(String s, byte[] bytes) {

        try {
            return objectMapper.readValue(bytes, UserSubscription.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }
    }
