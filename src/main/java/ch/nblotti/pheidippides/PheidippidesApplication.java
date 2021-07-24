package ch.nblotti.pheidippides;

import ch.nblotti.pheidippides.datasource.RoutingDataSource;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class})
@Slf4j
public class PheidippidesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PheidippidesApplication.class, args);

    }


    @Value("${spring.zookeeper.connect-string}")
    private String zkConnectString;



    @Value("${global.full-date-format}")
    public String messageDateFormat;


    @Value("${app.basic.flyway.package}")
    private String basicPackage;


    @Autowired
    private StateMachineFactory<STATES, EVENTS> stateMachineFactory;


    @Bean
    public DateTimeFormatter formatMessage() {
        return DateTimeFormatter.ofPattern(messageDateFormat);
    }


    @Bean("stateMachine")
    @Scope("singleton")
    public StateMachine<STATES, EVENTS> stateMachine() {

        return stateMachineFactory.getStateMachine();

    }

    @Bean
    @Scope("singleton")
    ZkClient zkClient() {

        return new ZkClient(zkConnectString, 12000, 10000, zkSerializer());
    }

    public ZkSerializer zkSerializer() {
        return new ZkSerializer() {

            @Override
            public byte[] serialize(Object data) throws ZkMarshallingError {
                try {
                    return ((String) data).getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                if (bytes == null)
                    return null;
                try {
                    return new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

      @Bean
    public DataSource createDefaultSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        return dataSource;
    }

    @Bean
    @Scope("singleton")
    public RoutingDataSource routingDatasource(StateMachine<STATES, EVENTS> stateMachine) {

        return new RoutingDataSource(createDefaultSource(), stateMachine);

    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(RoutingDataSource routingDatasource) {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDatasource);
        em.setPackagesToScan(new String[]{basicPackage});

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(RoutingDataSource routingDatasource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory(routingDatasource).getObject());

        return transactionManager;
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("spring.h2.console.enabled", "false");
        properties.setProperty("spring.jpa.hibernate.ddl-auto", "update");
        properties.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQL9Dialect");
        properties.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");


        return properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        stateMachine().sendEvent(EVENTS.EVENT_RECEIVED);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {


        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofMinutes(5))
                .build();
        return restTemplate;
    }

}
