package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import io.apicurio.registry.utils.serde.AbstractKafkaSerDe;
import io.apicurio.registry.utils.serde.JsonSchemaKafkaSerializer;
import io.apicurio.registry.utils.serde.JsonSchemaSerDeConstants;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachine;

import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class KafkaStreamManager {


    @NonNull
    StateMachine<STATES, EVENTS> stateMachine;


    @NonNull
    PheidippidesMonthlyTopology pheidippidesMonthlyTopology;

    @NonNull
    @Value("${spring.kafka.connect-string}")
    private String kafkaConnectString;
    @NonNull
    @Value("${apicurio.registry.url}")
    private String apicurioRegistryUrl;

    private KafkaStreams streams;


    public void doStartStream(ClientDTO clientDTO) throws IllegalStateException {

        if (streams == null || streams.state() == KafkaStreams.State.NOT_RUNNING) {

            Properties properties = initStreamConfig(clientDTO.getUserName());
            start(properties);
            this.stateMachine.sendEvent(EVENTS.SUCCESS);
        } else {
            log.info("Stream already running !");
            this.stateMachine.sendEvent(EVENTS.ERROR);
        }

    }

    public void doCloseStream(ClientDTO clientDTO)  {

        streams.close();
    }



        private void start(Properties streamsConfiguration) {


        streams = new KafkaStreams(pheidippidesMonthlyTopology.getTopology(), streamsConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();


    }

    private Properties initStreamConfig(String userName) {

        Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, userName);

        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectString);
        streamsConfiguration.put(AbstractKafkaSerDe.REGISTRY_URL_CONFIG_PARAM, apicurioRegistryUrl);

        streamsConfiguration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSchemaKafkaSerializer.class.getName());
        streamsConfiguration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSchemaKafkaSerializer.class.getName());

        streamsConfiguration.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonSchemaKafkaSerializer.class.getName());
        streamsConfiguration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSchemaKafkaSerializer.class.getName());
        streamsConfiguration.put(JsonSchemaSerDeConstants.REGISTRY_JSON_SCHEMA_VALIDATION_ENABLED, "true");

        return streamsConfiguration;
    }

}
