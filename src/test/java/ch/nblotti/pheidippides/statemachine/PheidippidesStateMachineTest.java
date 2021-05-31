package ch.nblotti.pheidippides.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PheidippidesStateMachine.class})
class PheidippidesStateMachineTest {


  @Autowired
  private StateMachineFactory<STATES, EVENTS> stateMachineFactory;


  private StateMachine<STATES, EVENTS> stateMachine;


  @BeforeEach
  public void setup() throws Exception {

    stateMachine = stateMachineFactory.getStateMachine();

    for (int i = 0; i < 10; i++) {
      if (stateMachine.getState() != null) {
        break;
      } else {
        Thread.sleep(200);
      }
    }

  }

  @Test
  public void testReady() throws Exception {

    StateMachineTestPlan<STATES, EVENTS> plan =
      StateMachineTestPlanBuilder.<STATES, EVENTS>builder()
        .stateMachine(stateMachine)
        .step()
        .expectState(STATES.READY)
        .expectStateMachineStopped(0)
        .and()
        .build();
    plan.test();

  }


}
