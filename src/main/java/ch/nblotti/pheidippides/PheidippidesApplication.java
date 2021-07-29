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
import java.nio.charset.StandardCharsets;
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

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {


        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofMinutes(5))
                .build();
    }

}
