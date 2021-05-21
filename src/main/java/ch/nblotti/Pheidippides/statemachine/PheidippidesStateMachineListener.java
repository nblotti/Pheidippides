package ch.nblotti.Pheidippides.statemachine;

import ch.nblotti.Pheidippides.client.ClientDTO;
import ch.nblotti.Pheidippides.client.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeader;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
@Slf4j
public class PheidippidesStateMachineListener {

  private final ClientService clientService;

  @Autowired
  public PheidippidesStateMachineListener(ClientService clientService) {
    this.clientService = clientService;
  }

  @StatesOnEntry(target = STATES.READY)
  public void ready(StateMachine<STATES, EVENTS> stateMachine) {

    stateMachine.sendEvent(EVENTS.SUCCESS);

  }

  @StatesOnEntry(target = STATES.INIT_ZOOKEEPER)
  public void initZookeeper() {

    clientService.subscribe();
  }

  @StatesOnEntry(target = STATES.INIT_DATABASE)
  public void initDatabase(StateMachine<STATES, EVENTS> stateMachine, @EventHeader ClientDTO clientDTO) {

    stateMachine.sendEvent(EVENTS.SUCCESS);
    log.info(String.format("Following client with id %s - managing %s strategies",clientDTO.getUserName(),clientDTO.getStrategies().size()));
  }

  @StatesOnEntry(target = STATES.INIT_STREAMS)
  public void initStreams(StateMachine<STATES, EVENTS> stateMachine) {

    stateMachine.sendEvent(EVENTS.SUCCESS);

  }


  @StatesOnEntry(target = STATES.WAIT_FOR_EVENT)
  public void waitForEvent() {

    //stateMachine.sendEvent(EVENTS.EVENT_RECEIVED);

  }

  @StatesOnEntry(target = STATES.TREATING_EVENT)
  public void treatingEvent(StateMachine<STATES, EVENTS> stateMachine,@EventHeader ClientDTO clientDTO) {

    stateMachine.sendEvent(EVENTS.EVENT_TREATED);
    log.info(String.format("Change detected in followed client (id %s) - now managing %s strategies",clientDTO.getUserName(),clientDTO.getStrategies().size()));
  }





  @OnTransition
  public void toError() {
  }

}
