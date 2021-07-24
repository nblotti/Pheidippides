package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KafkaConfiguration {


    @Value("${spring.kafka.connect-string}")
    private String kafkaConnectString;

    @Value("${apicurio.registry.url}")
    private String apicurioRegistryUrl;

    @Value("${app.stock_monthly_table}")
    private String quoteTopic;

    @Value("${app.stock_monthly_table_filtred}")
    private String quoteTopicFiltred;


    RestTemplate restTemplate;

    @Value("${app.kafka.connectors.stock.monthly.quote.url}")
    public String connectorQuoteUrl;
    @Value("${app.kafka.connect.url}")
    public String connectorUrl;
    @Value("${app.connector.payload.stock_monthly}")
    private String connectPayload;


    @Value("${app.user_quote_subscription_table}")
    public String userSubscriptionTopic;


    @Bean
    KafkaConnectManager kafkaConnectManager(RestTemplate restTemplate) {
        return new KafkaConnectManager(restTemplate, connectorQuoteUrl, connectorUrl, connectPayload, quoteTopicFiltred);
    }

    @Bean
    PheidippidesTopology pheidippidesTopology() {


        return new PheidippidesTopology(quoteTopic, quoteTopicFiltred, userSubscriptionTopic);
    }


    @Bean
    @Scope("singleton")
    KafkaStreamManager kafkaStreamManager(StateMachine<STATES, EVENTS> stateMachine, PheidippidesTopology pheidippidesTopology) {

        return new KafkaStreamManager(stateMachine, pheidippidesTopology, kafkaConnectString, userSubscriptionTopic, apicurioRegistryUrl);
    }


    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }


}
