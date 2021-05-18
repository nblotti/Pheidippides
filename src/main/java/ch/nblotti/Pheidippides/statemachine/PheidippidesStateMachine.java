package ch.nblotti.Pheidippides.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class PheidippidesStateMachine extends EnumStateMachineConfigurerAdapter<STATES, EVENTS> {


  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <STATES, EVENTS> config) throws Exception {
    config.withConfiguration()
      .autoStartup(true);

  }

  @Override
  public void configure(StateMachineStateConfigurer<STATES, EVENTS> states) throws Exception {
    states.withStates()
      .initial(STATES.READY)
      .state(STATES.INIT_ZOOKEEPER)
      .state(STATES.INIT_DATABASE)
      .state(STATES.INIT_STREAMS)
      .state(STATES.WAIT_FOR_EVENT)
      .state(STATES.ERROR)
      .end(STATES.DONE)
      .end(STATES.CANCELED);

  }


  @Override
  public void configure(StateMachineTransitionConfigurer<STATES, EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(STATES.READY).target(STATES.INIT_ZOOKEEPER).event(EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(STATES.INIT_ZOOKEEPER).target(STATES.INIT_DATABASE).event(EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(STATES.INIT_ZOOKEEPER).target(STATES.ERROR).event(EVENTS.ERROR)
      .and()
      .withExternal()
      .source(STATES.INIT_DATABASE).target(STATES.INIT_STREAMS).event(EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(STATES.INIT_DATABASE).target(STATES.ERROR).event(EVENTS.ERROR)
      .and()
      .withExternal()
      .source(STATES.INIT_STREAMS).target(STATES.ERROR).event(EVENTS.ERROR)
      .and()
      .withExternal()
      .source(STATES.INIT_STREAMS).target(STATES.WAIT_FOR_EVENT).event(EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(STATES.WAIT_FOR_EVENT).target(STATES.DONE).event(EVENTS.QUIT)
      .and()
      .withExternal()
      .source(STATES.ERROR).target(STATES.CANCELED);

  }


  public TaskExecutor myAsyncTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(5);
    taskExecutor.initialize();
    return taskExecutor;
  }


}

