package ch.nblotti.pheidippides.kafka.container;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class ContainerDeserializer implements Deserializer<Container> {


    @Override
    public Container deserialize(String s, byte[] in) {

        return SerializationUtils.deserialize(in);
    }

}
