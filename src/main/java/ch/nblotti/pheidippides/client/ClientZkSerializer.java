package ch.nblotti.pheidippides.client;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.StandardCharsets;

public class ClientZkSerializer implements ZkSerializer {

    @Override
    public byte[] serialize(Object data) throws ZkMarshallingError {


        return ((String) data).getBytes(StandardCharsets.UTF_8);

    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        if (bytes == null)
            return new byte[0];
        return new String(bytes, StandardCharsets.UTF_8);

    }

}
