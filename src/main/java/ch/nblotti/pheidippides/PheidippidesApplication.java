package ch.nblotti.pheidippides;

import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class})
@GeneratedExcludeJacocoTestCoverage
@Slf4j
public class PheidippidesApplication {

    @Autowired
    StateMachine<STATES, EVENTS> stateMachine;

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


    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        stateMachine.sendEvent(EVENTS.EVENT_RECEIVED);
    }

}
