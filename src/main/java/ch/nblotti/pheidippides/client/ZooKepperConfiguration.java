package ch.nblotti.pheidippides.client;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.nio.charset.StandardCharsets;

@GeneratedExcludeJacocoTestCoverage
@Configuration
public class ZooKepperConfiguration {


    @Value("${spring.zookeeper.connect-string}")
    private String zkConnectString;

    @Bean
    @Scope("singleton")
    ZkClient zkClient() {

        return new ZkClient(zkConnectString, 12000, 10000, zkSerializer());
    }

    public ZkSerializer zkSerializer() {
        return new ZkSerializer() {

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
        };
    }


}
