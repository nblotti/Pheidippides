package ch.nblotti.pheidippides.kafka.container;

import ch.nblotti.pheidippides.kafka.quote.QuoteWrapper;
import ch.nblotti.pheidippides.kafka.quote.SQL_OPERATION;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class ContainerDeserializer implements Deserializer<Container> {


    @Override
    public Container deserialize(String s, byte[] in) {

        return SerializationUtils.deserialize(in);
    }

}
