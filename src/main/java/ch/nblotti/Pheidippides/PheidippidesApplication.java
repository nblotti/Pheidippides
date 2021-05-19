package ch.nblotti.Pheidippides;

import ch.nblotti.Pheidippides.client.ClientDTO;
import ch.nblotti.Pheidippides.statemachine.EVENTS;
import ch.nblotti.Pheidippides.statemachine.STATES;
import org.I0Itec.zkclient.ZkClient;
import org.modelmapper.AbstractProvider;
import org.modelmapper.ModelMapper;
import org.modelmapper.Provider;
import org.modelmapper.spring.SpringIntegration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class PheidippidesApplication {

  public static void main(String[] args) {
    SpringApplication.run(PheidippidesApplication.class, args);
  }

  @Value("${spring.zookeeper.connect-string}")
  private String connectString;

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

  @Bean
  @Scope("singleton")
  ZkClient zkClient() {

    return new ZkClient(connectString, 12000, 3000);
  }

  @Bean
  ModelMapper modelMapper() {
    return new ModelMapper();
  }

}
