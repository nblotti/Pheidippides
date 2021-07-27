package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import io.apicurio.registry.utils.serde.AbstractKafkaSerDe;
import io.apicurio.registry.utils.serde.JsonSchemaKafkaSerializer;
import io.apicurio.registry.utils.serde.JsonSchemaSerDeConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class KafkaStreamManager {


    public static final String STOCK_MONTHLY_QUOTE = "stock_monthly_quote";
    public static final String INTERNAL_MAP = "%s_internal_map";
    public static final String INTERNAL_TRANSFORMED = "%s_internal_transformed";

    @NonNull
    StateMachine<STATES, EVENTS> stateMachine;


    @NonNull
    PheidippidesTopology pheidippidesTopology;

    @NonNull
    private String kafkaConnectString;

    @NonNull
    public String quoteTopicFiltred;

    @NonNull
    public String userSubscriptionTopic;

    @NonNull
    public String userSubscriptionTopicFiltred;

    @NonNull
    private String apicurioRegistryUrl;


    private KafkaStreams streams;


    public void doStartStream(ClientDTO clientDTO) throws IllegalStateException {

        if (streams == null || streams.state() == KafkaStreams.State.NOT_RUNNING) {

            Properties properties = initStreamConfig(clientDTO.getUserName());
            start(properties, clientDTO);
            this.stateMachine.sendEvent(EVENTS.SUCCESS);
        } else {
            log.info("Stream already running !");
            this.stateMachine.sendEvent(EVENTS.ERROR);
        }

    }

    public void doCloseStream(ClientDTO clientDTO) {

        streams.close();
    }


    public void deleteTopic(ClientDTO clientDTO) {

        String internalMapTopicName = String.format(INTERNAL_MAP, clientDTO.getUserName());
        String internalTransformedTopicName = String.format(INTERNAL_TRANSFORMED, clientDTO.getUserName());
        String userSubscriptionTopicName = String.format(userSubscriptionTopic, clientDTO.getUserName());
        String userSubscriptionTopicFiltredName = String.format(userSubscriptionTopicFiltred, clientDTO.getUserName());

        String quoteTopicFiltredName = String.format(quoteTopicFiltred, clientDTO.getUserName());


        Properties streamsConfiguration = initStreamConfig(clientDTO.getUserName());

        AdminClient client = AdminClient.create(streamsConfiguration);

        List<String> topics = Arrays.asList(STOCK_MONTHLY_QUOTE, quoteTopicFiltredName, internalMapTopicName, internalTransformedTopicName, userSubscriptionTopicName, userSubscriptionTopicFiltredName);
        client.deleteTopics(topics);
        client.close();
    }


    private void createTopic(Properties streamsConfiguration, String internalMapTopicName, String quoteTopicFiltredName, String internalTransformedTopicName, String userSubscriptionTopicName, String userSubscriptionTopicFiltred) {


        AdminClient client = AdminClient.create(streamsConfiguration);

        List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(STOCK_MONTHLY_QUOTE,
                Integer.parseInt("1"),
                Short.parseShort("1")));
        topics.add(new NewTopic(quoteTopicFiltredName,
                Integer.parseInt("1"),
                Short.parseShort("1")));
        topics.add(new NewTopic(internalMapTopicName,
                Integer.parseInt("1"),
                Short.parseShort("1")));
        topics.add(new NewTopic(internalTransformedTopicName,
                Integer.parseInt("1"),
                Short.parseShort("1")));
        topics.add(new NewTopic(userSubscriptionTopicFiltred,
                Integer.parseInt("1"),
                Short.parseShort("1")));

        topics.add(new NewTopic(userSubscriptionTopicName,
                Integer.parseInt("1"),
                Short.parseShort("1")));

        client.createTopics(topics);
        client.close();
    }


    private void start(Properties streamsConfiguration, ClientDTO clientDTO) {

        //Topic initialisation

        String internalMapTopicName = String.format(INTERNAL_MAP, clientDTO.getUserName());
        String internalTransformedTopicName = String.format(INTERNAL_TRANSFORMED, clientDTO.getUserName());
        String userSubscriptionTopicName = String.format(userSubscriptionTopic, clientDTO.getUserName());
        String userSubscriptionTopicFiltredName = String.format(userSubscriptionTopicFiltred, clientDTO.getUserName());
        String quoteTopicFiltredName = String.format(quoteTopicFiltred, clientDTO.getUserName());

        createTopic(streamsConfiguration, quoteTopicFiltredName,internalMapTopicName, internalTransformedTopicName, userSubscriptionTopicName, userSubscriptionTopicFiltredName);

        streams = new KafkaStreams(pheidippidesTopology.getTopology(clientDTO, streamsConfiguration, internalMapTopicName, internalTransformedTopicName, userSubscriptionTopicName), streamsConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();


    }

    private Properties initStreamConfig(String userName) {

        Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, userName);

        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectString);
        streamsConfiguration.put(AbstractKafkaSerDe.REGISTRY_URL_CONFIG_PARAM, apicurioRegistryUrl);

        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        streamsConfiguration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSchemaKafkaSerializer.class.getName());
        streamsConfiguration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSchemaKafkaSerializer.class.getName());
        streamsConfiguration.put(JsonSchemaSerDeConstants.REGISTRY_JSON_SCHEMA_VALIDATION_ENABLED, "true");

        return streamsConfiguration;
    }

}
