package ch.nblotti.pheidippides.statemachine;

import ch.nblotti.pheidippides.client.Client;
import ch.nblotti.pheidippides.client.ClientService;
import ch.nblotti.pheidippides.datasource.RoutingDataSource;
import ch.nblotti.pheidippides.kafka.KafkaConnectManager;
import ch.nblotti.pheidippides.kafka.KafkaStreamManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeader;

import java.util.HashMap;
import java.util.Map;

import static ch.nblotti.pheidippides.statemachine.PheidippidesStateMachineListener.CURRENT_CLIENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PheidippidesStateMachineListenerTest {


    PheidippidesStateMachineListener pheidippidesStateMachineListener;


    @Mock
    private ClientService clientService;

    @Mock
    private RoutingDataSource routingDataSource;

    @Mock
    private KafkaConnectManager kafkaConnectManager;

    @Mock
    private KafkaStreamManager kafkaStreamManager;

    @BeforeEach
    void setUp() {

        pheidippidesStateMachineListener = Mockito.spy(new PheidippidesStateMachineListener(clientService, routingDataSource, kafkaConnectManager, kafkaStreamManager));
    }

    @Test
    void waitForClient() {

        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);

        ArgumentCaptor<EVENTS> events = ArgumentCaptor.forClass(EVENTS.class);

        pheidippidesStateMachineListener.waitForClient(stateMachine);
        verify(stateMachine, times(1)).sendEvent(events.capture());

        assertEquals(EVENTS.SUCCESS, events.getValue());
    }


    @Test
    void initZookeeper() {

        doNothing().when(clientService).subscribe();

        pheidippidesStateMachineListener.initZookeeper();
        verify(clientService, times(1)).subscribe();

    }

    @Test
    void initDatabase() {

        Client newClient = mock(Client.class);
        doNothing().when(routingDataSource).createDataSource(newClient);

        pheidippidesStateMachineListener.initDatabase(newClient);
        verify(routingDataSource, times(1)).createDataSource(newClient);


    }

    @Test
    void initStreams() {

        Client newClient = mock(Client.class);
        ResponseEntity<String> re = mock(ResponseEntity.class);
        ExtendedState extendedState = mock(ExtendedState.class);

        Map<Object, Object> objectMap = new HashMap<>();
        when(extendedState.getVariables()).thenReturn(objectMap);

        doReturn(re).when(kafkaConnectManager).initStockConnector(newClient);
        doNothing().when(kafkaStreamManager).doStartStream(newClient);

        pheidippidesStateMachineListener.initStreams(newClient, extendedState);

        verify(kafkaConnectManager, times(1)).initStockConnector(newClient);
        verify(kafkaStreamManager, times(1)).doStartStream(newClient);
        assertEquals(newClient, objectMap.get(CURRENT_CLIENT));
    }

    @Test
    void treatingZKClientEventFollowedClient() {

        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);
        ExtendedState extendedState = mock(ExtendedState.class);
        Map<Object, Object> objectMap = new HashMap<>();
        Client newClient = mock(Client.class);


        when(extendedState.getVariables()).thenReturn(objectMap);

        objectMap.put(CURRENT_CLIENT, newClient);

        doReturn(Boolean.TRUE).when(kafkaConnectManager).deleteStockConnector(newClient);
        doNothing().when(kafkaStreamManager).deleteTopic(newClient);
        doNothing().when(kafkaStreamManager).doCloseStream();
        doNothing().when(clientService).unSubscribe(newClient);


        pheidippidesStateMachineListener.treatingZKClientEvent(stateMachine, extendedState, true);

        verify(kafkaConnectManager, times(1)).deleteStockConnector(newClient);
        verify(kafkaStreamManager, times(1)).deleteTopic(newClient);
        verify(kafkaStreamManager, times(1)).doCloseStream();
        verify(clientService, times(1)).unSubscribe(newClient);


    }

    @Test
    void treatingZKClientEventNotFollowedClient() {

        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);
        ExtendedState extendedState = mock(ExtendedState.class);
        Map<Object, Object> objectMap = new HashMap<>();
        Client newClient = mock(Client.class);


        when(extendedState.getVariables()).thenReturn(objectMap);

        objectMap.put(CURRENT_CLIENT, newClient);

        doNothing().when(kafkaStreamManager).doCloseStream();
        doNothing().when(clientService).unSubscribe(newClient);


        pheidippidesStateMachineListener.treatingZKClientEvent(stateMachine, extendedState, false);

        verify(kafkaConnectManager, times(0)).deleteStockConnector(newClient);
        verify(kafkaStreamManager, times(0)).deleteTopic(newClient);
        verify(kafkaStreamManager, times(1)).doCloseStream();
        verify(clientService, times(1)).unSubscribe(newClient);


    }

    @Test
    void treatingZKStrategiesEvent() {

        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);
        ExtendedState extendedState = mock(ExtendedState.class);
        Map<Object, Object> objectMap = new HashMap<>();
        Client newClient = mock(Client.class);
        ArgumentCaptor<EVENTS> events = ArgumentCaptor.forClass(EVENTS.class);

        when(extendedState.getVariables()).thenReturn(objectMap);

        pheidippidesStateMachineListener.treatingZKStrategiesEvent(stateMachine, newClient, extendedState);

        verify(stateMachine, times(1)).sendEvent(events.capture());
        assertEquals(EVENTS.EVENT_TREATED, events.getValue());
    }

    @Test
    void treatingZKDBEvent() {

        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);
        ExtendedState extendedState = mock(ExtendedState.class);
        Map<Object, Object> objectMap = new HashMap<>();
        Client newClient = mock(Client.class);
        ArgumentCaptor<EVENTS> events = ArgumentCaptor.forClass(EVENTS.class);
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);

        when(extendedState.getVariables()).thenReturn(objectMap);

        doNothing().when(kafkaStreamManager).doCloseStream();
        doReturn(true).when(kafkaConnectManager).deleteStockConnector(any());

        doNothing().when(routingDataSource).createDataSource(any());
        doReturn(responseEntity).when(kafkaConnectManager).initStockConnector(any());
        doNothing().when(kafkaStreamManager).doStartStream(any());
        pheidippidesStateMachineListener.treatingZKDBEvent(stateMachine, newClient, extendedState);

        verify(stateMachine, times(1)).sendEvent(events.capture());
        assertEquals(EVENTS.EVENT_TREATED, events.getValue());

    }

    @Test
    void treatingZKDBEventException() {

        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);
        ExtendedState extendedState = mock(ExtendedState.class);
        Map<Object, Object> objectMap = new HashMap<>();
        Client newClient = mock(Client.class);
        ArgumentCaptor<EVENTS> events = ArgumentCaptor.forClass(EVENTS.class);
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);

        when(extendedState.getVariables()).thenReturn(objectMap);

        doNothing().when(kafkaStreamManager).doCloseStream();
        doReturn(true).when(kafkaConnectManager).deleteStockConnector(any());

        doNothing().when(routingDataSource).createDataSource(any());
        doReturn(responseEntity).when(kafkaConnectManager).initStockConnector(any());
        doThrow(IllegalStateException.class).when(kafkaStreamManager).doStartStream(any());
        pheidippidesStateMachineListener.treatingZKDBEvent(stateMachine, newClient, extendedState);

        verify(stateMachine, times(1)).sendEvent(events.capture());
        assertEquals(EVENTS.ERROR, events.getValue());

    }


    @Test
    void toError() {
        StateMachine<STATES, EVENTS> stateMachine = mock(StateMachine.class);
        pheidippidesStateMachineListener.toError(stateMachine);
        verify(stateMachine, times(1)).stop();
    }

}