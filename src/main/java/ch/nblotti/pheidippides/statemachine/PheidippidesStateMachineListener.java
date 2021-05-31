package ch.nblotti.pheidippides.statemachine;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.client.ClientService;
import ch.nblotti.pheidippides.datasource.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeader;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
@Slf4j
public class PheidippidesStateMachineListener {

  private final ClientService clientService;

  private final RoutingDataSource routingDataSource;

  @Autowired
  public PheidippidesStateMachineListener(ClientService clientService, RoutingDataSource routingDataSource) {
    this.clientService = clientService;
    this.routingDataSource = routingDataSource;
  }


  @StatesOnEntry(target = STATES.INIT_ZOOKEEPER)
  public void initZookeeper() {

    clientService.subscribe();
  }

  @StatesOnEntry(target = STATES.INIT_DATABASE)
  public void initDatabase(StateMachine<STATES, EVENTS> stateMachine, @EventHeader ClientDTO clientDTO) {


    try {
      routingDataSource.createDataSource(clientDTO.getDbUrl(), clientDTO.getDbUser(), clientDTO.getDbPassword());
    } catch (Exception e) {
      stateMachine.sendEvent(EVENTS.ERROR);
      return;
    }

    stateMachine.sendEvent(EVENTS.SUCCESS);
    log.info(String.format("Following client with id %s - managing %s strategies", clientDTO.getUserName(), clientDTO.getStrategies().size()));
  }

  @StatesOnEntry(target = STATES.INIT_STREAMS)
  public void initStreams(StateMachine<STATES, EVENTS> stateMachine) {
    stateMachine.sendEvent(EVENTS.SUCCESS);

  }


  @StatesOnEntry(target = STATES.WAIT_FOR_EVENT)
  public void waitForEvent(StateMachine<STATES, EVENTS> stateMachine) {


  }

  @StatesOnEntry(target = STATES.TREATING_ZK_STRATEGIES_EVENT)
  public void treatingZKStrategiesEvent(StateMachine<STATES, EVENTS> stateMachine, @EventHeader ClientDTO clientDTO) {

    stateMachine.sendEvent(EVENTS.EVENT_TREATED);
    log.info(String.format("Change detected in followed client (id %s) - now managing %s strategies", clientDTO.getUserName(), clientDTO.getStrategies().size()));
  }


  @StatesOnEntry(target = STATES.TREATING_ZK_DB_EVENT)
  public void treatingZKDBEvent(StateMachine<STATES, EVENTS> stateMachine, @EventHeader ClientDTO clientDTO) {

    try {
      routingDataSource.createDataSource(clientDTO.getDbUrl(), clientDTO.getDbUser(), clientDTO.getDbPassword());
    } catch (Exception e) {
      stateMachine.sendEvent(EVENTS.ERROR);
      return;
    }
    stateMachine.sendEvent(EVENTS.EVENT_TREATED);
    log.info(String.format("Change detected in followed client (id %s) - now managing %s strategies", clientDTO.getUserName(), clientDTO.getStrategies().size()));
  }


  @StatesOnEntry(target = STATES.ERROR)
  public void toError(StateMachine<STATES, EVENTS> stateMachine) {
    log.info(String.format("Error, quitting"));
    stateMachine.stop();

  }

}
