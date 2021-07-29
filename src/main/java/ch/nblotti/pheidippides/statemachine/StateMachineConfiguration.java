package ch.nblotti.pheidippides.statemachine;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import ch.nblotti.pheidippides.datasource.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import javax.sql.DataSource;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@GeneratedExcludeJacocoTestCoverage
@Configuration
public class StateMachineConfiguration {



    @Value("${global.full-date-format}")
    public String messageDateFormat;

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


    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        stateMachine().sendEvent(EVENTS.EVENT_RECEIVED);
    }

}
