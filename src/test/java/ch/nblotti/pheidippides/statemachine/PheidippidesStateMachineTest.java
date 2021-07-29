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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;


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

        await().atMost(10, TimeUnit.SECONDS).until(didTheThing());  // Compliant


    }

    private Callable<Boolean> didTheThing() {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return stateMachine.getState() != null;
            }
        };
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
