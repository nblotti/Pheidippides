package ch.nblotti.pheidippides.client;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
        return new ClientZkSerializer();
    }


}
