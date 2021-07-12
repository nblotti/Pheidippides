package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.MonthlyQuoteFilter;
import ch.nblotti.pheidippides.kafka.quote.QuoteDeserializer;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String connectorMonthlyQuoteUrl;
    @Value("${app.kafka.connect.url}")
    public String connectorUrl;
    @Value("${app.connector.payload.stock_weekly}")
    private String weeklyConnectPayload;
    @Value("${app.connector.payload.stock_monthly}")
    private String monthlyConnectPayload;

    @Value("${app.stock_monthly_table}")
    public String monthlyQuoteTopic;


    @Bean
    MonthlyQuoteFilter monthlyQuoteFilter(QuoteDeserializer quoteDeserializer) {

        return new MonthlyQuoteFilter(quoteDeserializer);
    }

    @Bean
    QuoteDeserializer quoteDeserializer() {
        return new QuoteDeserializer();
    }


    @Bean
    KafkaConnectManager kafkaConnectManager(RestTemplate restTemplate) {
        return new KafkaConnectManager(restTemplate, connectorMonthlyQuoteUrl, connectorUrl, weeklyConnectPayload, monthlyConnectPayload, monthlyQuoteTopic);
    }

    @Bean
    PheidippidesMonthlyTopology pheidippidesMonthlyTopology(MonthlyQuoteFilter monthlyQuoteFilter) {

        return new PheidippidesMonthlyTopology(monthlyQuoteFilter, quoteTopic, quoteTopicFiltred);
    }


    @Bean
    @Scope("singleton")
    KafkaStreamManager kafkaStreamManager(StateMachine<STATES, EVENTS> stateMachine, PheidippidesMonthlyTopology pheidippidesMonthlyTopology) {

        return new KafkaStreamManager(stateMachine, pheidippidesMonthlyTopology, kafkaConnectString, apicurioRegistryUrl);
    }


    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }


}
