package ch.nblotti.pheidippides.statemachine;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.time.format.DateTimeFormatter;

@GeneratedExcludeJacocoTestCoverage
@Configuration
public class StateMachineConfiguration {


    @Value("${global.full-date-format}")
    public String messageDateFormat;


    @Bean
    public DateTimeFormatter formatMessage() {
        return DateTimeFormatter.ofPattern(messageDateFormat);
    }


    @Bean("stateMachine")
    @Scope("singleton")
    public StateMachine<STATES, EVENTS> stateMachine(StateMachineFactory<STATES, EVENTS> stateMachineFactory) {

        return stateMachineFactory.getStateMachine();

    }


}
