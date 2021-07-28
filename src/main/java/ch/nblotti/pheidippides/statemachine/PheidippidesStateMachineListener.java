package ch.nblotti.pheidippides.statemachine;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.client.ClientService;
import ch.nblotti.pheidippides.datasource.RoutingDataSource;
import ch.nblotti.pheidippides.kafka.KafkaConnectManager;
import ch.nblotti.pheidippides.kafka.KafkaStreamManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeader;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
@Slf4j
@AllArgsConstructor
public class PheidippidesStateMachineListener {

    public static final String CURRENT_CLIENT = "currentClient";
    private final ClientService clientService;

    private final RoutingDataSource routingDataSource;
    private final KafkaConnectManager kafkaConnectManager;
    private final KafkaStreamManager kafkaStreamManager;


    @StatesOnEntry(target = STATES.NO_FREE_CLIENT)
    public void waitForClient(StateMachine<STATES, EVENTS> stateMachine) {

        try {
            log.warn("No client available, retrying in 10s");
            Thread.sleep(10000);
        } catch (InterruptedException e) {

            log.info(e.getMessage());
            Thread.currentThread().interrupt();

        }
        stateMachine.sendEvent(EVENTS.SUCCESS);
    }


    @StatesOnEntry(target = STATES.INIT_ZOOKEEPER)
    public void initZookeeper() {

        clientService.subscribe();
    }

    @StatesOnEntry(target = STATES.INIT_DATABASE)
    public void initDatabase(@EventHeader ClientDTO newClient) {

        routingDataSource.createDataSource(newClient);

        log.info(String.format("Following client with id %s - managing %s strategies", newClient.getUserName(), newClient.getStrategies().size()));
    }

    @StatesOnEntry(target = STATES.INIT_STREAMS)
    public void initStreams(@EventHeader ClientDTO newClient, ExtendedState extendedState) {

        extendedState.getVariables().put(CURRENT_CLIENT, newClient);
        kafkaConnectManager.initStockConnector(newClient);
        kafkaStreamManager.doStartStream(newClient);

    }


    @StatesOnEntry(target = STATES.TREATING_ZK_CLIENT_CHANGE_EVENT)
    public void treatingZKClientEvent(StateMachine<STATES, EVENTS> stateMachine, ExtendedState extendedState, @EventHeader Boolean followedClient) {
        ClientDTO clientDTO = (ClientDTO) extendedState.getVariables().get(CURRENT_CLIENT);


        if (followedClient) {
            kafkaConnectManager.deleteStockConnector(clientDTO);
            kafkaStreamManager.deleteTopic(clientDTO);
        }
        kafkaStreamManager.doCloseStream();
        clientService.unSubscribe(clientDTO);


        log.info(String.format("Change detected in clients, closing connnection and restarting client election process"));
        stateMachine.sendEvent(EVENTS.EVENT_TREATED);
    }


    @StatesOnEntry(target = STATES.TREATING_ZK_STRATEGIES_EVENT)
    public void treatingZKStrategiesEvent(StateMachine<STATES, EVENTS> stateMachine, @EventHeader ClientDTO newClient, ExtendedState extendedState) {

        extendedState.getVariables().put(CURRENT_CLIENT, newClient);
        stateMachine.sendEvent(EVENTS.EVENT_TREATED);
        log.info(String.format("Change detected in followed client (id %s) - now managing %s strategies", newClient.getUserName(), newClient.getStrategies().size()));
    }


    @StatesOnEntry(target = STATES.TREATING_ZK_DB_EVENT)
    public void treatingZKDBEvent(StateMachine<STATES, EVENTS> stateMachine, @EventHeader ClientDTO newClient, ExtendedState extendedState) {

        try {
            ClientDTO oldClient = (ClientDTO) extendedState.getVariables().get(CURRENT_CLIENT);
            kafkaStreamManager.doCloseStream();
            kafkaConnectManager.deleteStockConnector(oldClient);

            routingDataSource.createDataSource(newClient);
            kafkaConnectManager.initStockConnector(newClient);
            kafkaStreamManager.doStartStream(newClient);


            extendedState.getVariables().put(CURRENT_CLIENT, newClient);
        } catch (Exception e) {
            stateMachine.sendEvent(EVENTS.ERROR);
            return;
        }
        stateMachine.sendEvent(EVENTS.EVENT_TREATED);
        log.info(String.format("Change detected in followed client (id %s) - now managing %s strategies", newClient.getUserName(), newClient.getStrategies().size()));
    }


    @StatesOnEntry(target = STATES.ERROR)
    public void toError(StateMachine<STATES, EVENTS> stateMachine) {
        log.info(String.format("Error, quitting"));
        stateMachine.stop();

    }

}
