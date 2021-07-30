package ch.nblotti.pheidippides.client;

import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.nblotti.pheidippides.client.ClientService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ClientServiceTest {


    ClientService clientService;
    ZkClient zkClient;

    StateMachine<STATES, EVENTS> stateMachine;


    @BeforeEach
    void beforeEach() {
        zkClient = Mockito.mock(ZkClient.class);
        stateMachine = Mockito.mock(StateMachine.class);
        clientService = Mockito.spy(new ClientService(zkClient, stateMachine));
    }


    @Test
    void subscribe() {

        // get the first free client
        String clientName = "test";
        List<StrategiesDTO> strategies = Mockito.mock(List.class);

        doReturn(clientName).when(clientService).selectFreeClient();

        // register as a client listener in Zookeeper
        doNothing().when(clientService).registerToClientChanges(clientName);


        // read client database info and send a message to subscribers
        ClientDBInfo clientDBInfo = clientService.readDBInfo(clientName);

        // read client strategy related info and send a message to subscribers
        doReturn(strategies).when(clientService).chooseStrategy(clientName);

        clientService.subscribe();


        verify(clientService, times(1)).selectFreeClient();
        verify(clientService, times(1)).registerToClientChanges(clientName);
        verify(clientService, times(1)).buildAndSendUpdatedMessage(clientName, EVENTS.SUCCESS);


    }

    @Test
    void subscribeClientNull() {


        List<StrategiesDTO> strategies = Mockito.mock(List.class);

        doReturn(null).when(clientService).selectFreeClient();

        clientService.subscribe();

        verify(stateMachine, times(1)).sendEvent(EVENTS.WAIT_FOR_CLIENT);
        verify(clientService, times(1)).selectFreeClient();
        verify(clientService, times(0)).registerToClientChanges(anyString());
        verify(clientService, times(0)).buildAndSendUpdatedMessage(anyString(), any());


    }

    @Test
    void unSubscribe() {
        String clientName = "test";
        Client client = mock(Client.class);

        when(client.getUserName()).thenReturn(clientName);

        clientService.unSubscribe(client);

        verify(zkClient, times(1)).unsubscribeAll();
        verify(clientService, times(1)).removeFromClientLiveNodes(clientName);


    }


    @Test
    void registerToClientChanges() {

        String clientName = "test";


        doNothing().when(clientService).addToClientLiveNodes(clientName);
        doNothing().when(clientService).registerToStrategyChanges(clientName);
        doNothing().when(clientService).registerTODBInfoChangeEvent(clientName);


        clientService.registerToClientChanges(clientName);

        verify(clientService, times(1)).addToClientLiveNodes(clientName);
        verify(clientService, times(1)).registerToStrategyChanges(clientName);
        verify(clientService, times(1)).registerTODBInfoChangeEvent(clientName);

    }


    @Test
    void findAllClientNoCLient() {


        List<String> clients = new ArrayList<>();

        doReturn(clients).when(clientService).findAllClient();


        String returned = clientService.selectFreeClient();

        Assert.assertNull(returned);

    }

    @Test
    void findAllClientNoCLientWithNoZeroNodeAllowed() {


        String firstClient = "1";
        List<String> clients = new ArrayList<>();
        clients.add(firstClient);

        List<String> returnedFreeClient = mock(List.class);
        when(returnedFreeClient.size()).thenReturn(1);
        doReturn(clients).when(clientService).findAllClient();

        doReturn(returnedFreeClient).when(clientService).getLiveNodes(firstClient);

        doReturn(0).when(clientService).getNodeAllowed(firstClient);


        String returned = clientService.selectFreeClient();

        Assert.assertNull(returned);

    }

    @Test
    void selectFreeClientOneClientLiveNodesZero() {

        String firstClient = "1";
        String secondClient = "2";
        List<String> clients = new ArrayList<>();
        clients.add(firstClient);
        clients.add(secondClient);

        List<String> returnedFreeClient = mock(List.class);
        when(returnedFreeClient.isEmpty()).thenReturn(true);
        doReturn(clients).when(clientService).findAllClient();

        doReturn(returnedFreeClient).when(clientService).getLiveNodes(secondClient);

        doReturn(0).when(clientService).getNodeAllowed(firstClient);
        doReturn(1).when(clientService).getNodeAllowed(secondClient);


        String returned = clientService.selectFreeClient();

        Assert.assertEquals(secondClient, returned);

    }

    @Test
    void selectFreeClientLiveNodesZero() {

        String firstClient = "1";
        String secondClient = "2";
        String thirdClient = "3";
        String fourthClient = "4";
        List<String> clients = new ArrayList<>();
        clients.add(firstClient);
        clients.add(secondClient);
        clients.add(thirdClient);
        clients.add(fourthClient);

        List<String> returnedNotFreeClient = mock(List.class);
        List<String> returnedFreeClient = mock(List.class);
        when(returnedNotFreeClient.size()).thenReturn(5);
        when(returnedFreeClient.size()).thenReturn(0);
        doReturn(clients).when(clientService).findAllClient();

        doReturn(returnedNotFreeClient).when(clientService).getLiveNodes(firstClient);
        doReturn(returnedNotFreeClient).when(clientService).getLiveNodes(secondClient);
        doReturn(returnedFreeClient).when(clientService).getLiveNodes(thirdClient);

        doReturn(1).when(clientService).getNodeAllowed(firstClient);
        doReturn(1).when(clientService).getNodeAllowed(secondClient);
        doReturn(1).when(clientService).getNodeAllowed(thirdClient);


        String returned = clientService.selectFreeClient();

        Assert.assertEquals(thirdClient, returned);

    }

    @Test
    void selectFreeNoClientLiveNodesZero() {

        String firstClient = "1";
        String secondClient = "2";
        String thirdClient = "3";
        String fourthClient = "4";
        List<String> clients = new ArrayList<>();
        clients.add(firstClient);
        clients.add(secondClient);
        clients.add(thirdClient);
        clients.add(fourthClient);

        List<String> returnedNotFreeClient = mock(List.class);
        doReturn(clients).when(clientService).findAllClient();

        doReturn(returnedNotFreeClient).when(clientService).getLiveNodes(firstClient);
        doReturn(returnedNotFreeClient).when(clientService).getLiveNodes(secondClient);
        doReturn(returnedNotFreeClient).when(clientService).getLiveNodes(thirdClient);
        doReturn(returnedNotFreeClient).when(clientService).getLiveNodes(fourthClient);


        doReturn(1).when(clientService).getLiveNodesCount(firstClient);
        doReturn(1).when(clientService).getNodeAllowed(firstClient);
        doReturn(1).when(clientService).getLiveNodesCount(secondClient);
        doReturn(1).when(clientService).getNodeAllowed(secondClient);
        doReturn(1).when(clientService).getLiveNodesCount(thirdClient);
        doReturn(1).when(clientService).getNodeAllowed(thirdClient);
        doReturn(1).when(clientService).getLiveNodesCount(fourthClient);
        doReturn(2).when(clientService).getNodeAllowed(fourthClient);

        String returned = clientService.selectFreeClient();

        Assert.assertEquals(fourthClient, returned);

    }


    @Test
    void registerNode() {

        String clientStr = "first";
        String UUID = "UUID";


        doReturn(UUID).when(clientService).getUuid();

        String path = String.format(CLIENT_LIVE_NODES + "/%s", clientStr, UUID);

        doNothing().when(clientService).addToLiveNodes(path);
        clientService.addToClientLiveNodes(clientStr);

        verify(clientService, times(1)).addToLiveNodes(path);

    }

    @Test
    void getNullIndex() {

        List<String> liveNodes = new ArrayList<>();

        String firstEntry = "1";
        String secondEntry = "3";
        String thirdEntry = "2";
        liveNodes.add(firstEntry);

        liveNodes.add(secondEntry);
        liveNodes.add(thirdEntry);

        doReturn(liveNodes).when(clientService).getLiveNodes(anyString());

        int returned = clientService.getLiveNodesIndex(firstEntry);

        assertEquals(-1, returned);
    }

    @Test
    void getIndex() {


        List<String> liveNodes = new ArrayList<>();
        String firstEntry = "1";
        String secondEntry = "3";
        String thirdEntry = "2";
        liveNodes.add(firstEntry);

        liveNodes.add(secondEntry);
        liveNodes.add(thirdEntry);

        doReturn(liveNodes).when(clientService).getAllChildren(anyString());
        doReturn(secondEntry).when(clientService).getUuid();
        int returned = clientService.getLiveNodesIndex(secondEntry);

        assertEquals(2, returned);
    }

    @Test
    void getAllNodesChildren() {
        String pathStr = "/current/path";
        List<String> returnStr = Arrays.asList("1", "2", "3", "4");

        doReturn(Boolean.TRUE).when(zkClient).exists(pathStr);
        doReturn(returnStr).when(zkClient).getChildren(pathStr);

        List<String> result = clientService.getAllChildren(pathStr);

        Assert.assertEquals(result, returnStr);

    }

    @Test
    void getAllNodesChildrenNoPath() {
        String pathStr = "/current/path";
        List<String> returnStr = Arrays.asList("1", "2", "3", "4");

        doReturn(Boolean.FALSE).when(zkClient).exists(pathStr);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            List<String> result = clientService.getAllChildren(pathStr);
        });

        assertTrue(exception.getMessage().contains("No node /allNodes exists"));


    }


    @Test
    void readNodeData() {

        String path = "path";

        doReturn(path).when(zkClient).readData(path, true);

        String result = clientService.readNodeData(path);

        verify(zkClient, times(1)).readData(path, true);

    }


    @Test
    void registerChildrenChangeWatcher() {
        List<String> toReturn = mock(List.class);
        String clientStr = "client";
        IZkChildListener iZkChildListener = mock(IZkChildListener.class);

        doReturn(toReturn).when(zkClient).subscribeChildChanges(anyString(), any(IZkChildListener.class));

        clientService.registerToStrategyChanges(clientStr);

        verify(zkClient, times(3)).subscribeChildChanges(anyString(), any(IZkChildListener.class));


    }

    @Test
    void closeConnection() {
        clientService.closeConnection();

        verify(zkClient, times(1)).close();

    }

    @Test
    void readDBInfoAndSendDBChangeInfo() {
        String clientName = "test";
        String dbUrlStr = String.format(CLIENT_DB_URL, clientName);
        String dbUrl = "jdbc://";
        String dbUserStr = String.format(CLIENT_DB_USER, clientName);
        String dbUser = "testuser";
        String dbPasswordStr = String.format(CLIENT_DB_PASSWORD, clientName);
        String dbPassword = "testpassword";


        doReturn(dbUrl).when(clientService).readNodeData(dbUrlStr);
        doReturn(dbUser).when(clientService).readNodeData(dbUserStr);
        doReturn(dbPassword).when(clientService).readNodeData(dbPasswordStr);

        ClientDBInfo dbInfo = clientService.readDBInfo(clientName);


        Assert.assertEquals(dbInfo.getDbUrl(), dbUrl);
        Assert.assertEquals(dbInfo.getDbUser(), dbUser);
        Assert.assertEquals(dbInfo.getDbPassword(), dbPassword);

    }

    @Test
    void chooseStrategyNoStrategyForClient() {

        String clientName = "test";
        int position = 1;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 0;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 1;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 0;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);


        Exception exception = assertThrows(IllegalStateException.class, () -> {
            clientService.chooseStrategy(clientName);
        });
    }

    @Test
    void chooseStrategyOneNodeTwoStrategy() {

        String clientName = "test";
        int position = 1;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 0;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 1;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 2;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);


        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";

        strategies.add(firstStrategy);
        strategies.add(secondStrategy);

        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(2, returned.size());
        assertEquals(firstStrategy, returned.get(0).getStrategyName());
        assertEquals(secondStrategy, returned.get(1).getStrategyName());
    }


    @Test
    void chooseTwoStrategyTwoNodeFirstPosition() {


        String clientName = "test";
        int position = 1;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 2;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 2;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 2;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);


        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";

        strategies.add(firstStrategy);
        strategies.add(secondStrategy);

        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(1, returned.size());
        assertEquals(firstStrategy, returned.get(0).getStrategyName());

    }

    @Test
    void chooseTwoStrategyTwoNodeSecondPosition() {

        String clientName = "test";
        int position = 2;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 2;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 2;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 2;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);


        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";

        strategies.add(firstStrategy);
        strategies.add(secondStrategy);

        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(1, returned.size());
        assertEquals(secondStrategy, returned.get(0).getStrategyName());
    }


    @Test
    void chooseThreeStrategyTwoNodeFirstPosition() {

        String clientName = "test";
        int position = 1;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 2;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 2;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 3;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);


        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";
        String thirdStrategy = "thirdStrategy";

        strategies.add(firstStrategy);
        strategies.add(secondStrategy);
        strategies.add(thirdStrategy);

        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(1, returned.size());
        assertEquals(firstStrategy, returned.get(0).getStrategyName());
    }


    @Test
    void chooseThreeStrategyTwoNodeLastPosition() {

        String clientName = "test";
        int position = 2;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 2;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 2;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 3;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);


        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";
        String thirdStrategy = "thirdStrategy";

        strategies.add(firstStrategy);
        strategies.add(secondStrategy);
        strategies.add(thirdStrategy);

        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(2, returned.size());
        assertEquals(secondStrategy, returned.get(0).getStrategyName());
        assertEquals(thirdStrategy, returned.get(1).getStrategyName());
    }

    @Test
    void chooseSevenStrategyThreeNodeLastPosition() {


        String clientName = "test";
        int position = 3;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 3;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 3;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 7;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);

        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";
        String thirdStrategy = "thirdStrategy";
        String fourthtrategy = "fourthtrategy";
        String fifthtrategy = "fifthtrategy";
        String sixthStrategy = "sixthStrategy";
        String seventhStrategy = "seventhStrategy";


        strategies.add(firstStrategy);
        strategies.add(secondStrategy);
        strategies.add(thirdStrategy);
        strategies.add(fourthtrategy);
        strategies.add(fifthtrategy);
        strategies.add(sixthStrategy);
        strategies.add(seventhStrategy);


        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(3, returned.size());
        assertEquals(fifthtrategy, returned.get(0).getStrategyName());
        assertEquals(sixthStrategy, returned.get(1).getStrategyName());
        assertEquals(seventhStrategy, returned.get(2).getStrategyName());


    }

    @Test
    void chooseSevenStrategyThreeNodeSecondPosition() {

        String clientName = "test";
        int position = 2;
        doReturn(position).when(clientService).getLiveNodesIndex(clientName);
        int liveNodeCount = 3;
        doReturn(liveNodeCount).when(clientService).getLiveNodesCount(clientName);

        int allowedNode = 3;
        doReturn(allowedNode).when(clientService).getNodeAllowed(clientName);

        int strategiesCount = 7;
        doReturn(strategiesCount).when(clientService).getStrategiesCount(clientName);

        List<String> strategies = new ArrayList<>();
        String firstStrategy = "firstStrategy";
        String secondStrategy = "secondStrategy";
        String thirdStrategy = "thirdStrategy";
        String fourthtrategy = "fourthtrategy";
        String fifthtrategy = "fifthtrategy";
        String sixthStrategy = "sixthStrategy";
        String seventhStrategy = "seventhStrategy";


        strategies.add(firstStrategy);
        strategies.add(secondStrategy);
        strategies.add(thirdStrategy);
        strategies.add(fourthtrategy);
        strategies.add(fifthtrategy);
        strategies.add(sixthStrategy);
        strategies.add(seventhStrategy);


        doReturn(strategies).when(clientService).getStrategies(clientName);


        List<StrategiesDTO> returned = clientService.chooseStrategy(clientName);

        assertEquals(2, returned.size());
        assertEquals(thirdStrategy, returned.get(0).getStrategyName());
        assertEquals(fourthtrategy, returned.get(1).getStrategyName());
    }


    @Test
    void getLiveNodesIndex() {

        String clientName = "2";
        List<String> liveNodes = new ArrayList<>();

        String firstEntry = "1";
        String secondEntry = "3";
        String thirdEntry = "2";
        liveNodes.add(firstEntry);

        liveNodes.add(secondEntry);
        liveNodes.add(thirdEntry);

        doReturn(liveNodes).when(clientService).getLiveNodes(clientName);
        doReturn(secondEntry).when(clientService).getUuid();

        int index = clientService.getLiveNodesIndex(clientName);

        assertEquals(2, index);
    }


    @Test
    void getLiveNodesNotInIndex() {

        String clientName = "out";
        List<String> liveNodes = new ArrayList<>();

        String firstEntry = "1";
        String secondEntry = "3";
        String thirdEntry = "2";
        String fourthEntry = "4";
        liveNodes.add(firstEntry);

        liveNodes.add(secondEntry);
        liveNodes.add(thirdEntry);

        doReturn(liveNodes).when(clientService).getLiveNodes(clientName);
        doReturn(fourthEntry).when(clientService).getUuid();

        int index = clientService.getLiveNodesIndex(clientName);

        assertEquals(-1, index);
    }

    @Test
    void getNodeAllowed() {
        String clientName = "clientName";
        String path = String.format(CLIENT_NODE_ALLOWED, clientName);

        doReturn("3").when(clientService).readNodeData(path);

        int returned = clientService.getNodeAllowed(clientName);
        Assert.assertEquals(3, returned);


    }

    @Test
    void getNodeAllowedException() {

        String clientName = "clientName";
        String path = String.format(CLIENT_NODE_ALLOWED, clientName);

        doReturn("").when(clientService).readNodeData(path);


        int returned = clientService.getNodeAllowed(clientName);

        Assert.assertEquals(0, returned);
    }

    @Test
    void removeFromClientLiveNodes() {

        String clientName = "test";
        String uuid = "uuid";

        doReturn(uuid).when(clientService).getUuid();

        clientService.removeFromClientLiveNodes(clientName);

        verify(clientService, times(1)).removeFromLiveNodes(String.format(CLIENT_LIVE_NODES + "/%s", clientName, uuid));
    }

    @Test
    void buildAndSendDeletedMessageListContainClientName() {

        String clientName = "test";
        List<String> clientNameList = Arrays.asList(clientName);

        clientService.buildAndSendDeletedMessage(clientNameList, clientName);

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

        verify(stateMachine, times(1)).sendEvent(messageArgumentCaptor.capture());

        Message mess = messageArgumentCaptor.getValue();

        Assert.assertEquals(EVENTS.ZK_CLIENT_CHANGE_EVENT_RECEIVED, mess.getPayload());

        Assert.assertEquals(false, mess.getHeaders().get(FOLLOWED_CLIENT));
    }

    @Test
    void buildAndSendDeletedMessageListNotContainClientName() {

        String clientName = "test";
        List<String> clientNameList = Arrays.asList(clientName);

        clientService.buildAndSendDeletedMessage(clientNameList, "DifferentClientName");


        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

        verify(stateMachine, times(1)).sendEvent(messageArgumentCaptor.capture());

        Message mess = messageArgumentCaptor.getValue();

        Assert.assertEquals(EVENTS.ZK_CLIENT_CHANGE_EVENT_RECEIVED, mess.getPayload());

        Assert.assertEquals(true, mess.getHeaders().get(FOLLOWED_CLIENT));
    }


    @Test
    void getStrategiesCount() {

        String clientName = "clientName";
        List<String> strategies = mock(List.class);

        doReturn(strategies).when(clientService).getStrategies(clientName);
        when(strategies.size()).thenReturn(3);

        assertEquals(3, clientService.getStrategiesCount(clientName));
    }

    @Test
    void registerToStrategyChanges() {

        String clientName = "clientName";

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

        String liveNodesPath = String.format(CLIENT_LIVE_NODES, clientName);
        String strategiesPath = String.format(CLIENT_STRATEGIES, clientName);
        String nodeAllowedPath = String.format(CLIENT_NODE_ALLOWED, clientName);

        clientService.registerToStrategyChanges(clientName);

        verify(zkClient, times(3)).subscribeChildChanges(pathCaptor.capture(), any(IZkChildListener.class));

        List<String> calls = pathCaptor.getAllValues();
        assertEquals(CLIENTS, calls.get(0));
        assertEquals(liveNodesPath, calls.get(1));
        assertEquals(strategiesPath, calls.get(2));

        ArgumentCaptor<String> pathdataCaptor = ArgumentCaptor.forClass(String.class);
        verify(zkClient, times(1)).subscribeDataChanges(pathdataCaptor.capture(), any(IZkDataListener.class));
        calls = pathdataCaptor.getAllValues();
        assertEquals(nodeAllowedPath, calls.get(0));
    }

    @Test
    void getZkChildDeleteListener() throws Exception {

        String clientName = "clientName";
        List<String> clientNameList = Arrays.asList(clientName);

        IZkChildListener iZkChildListener = clientService.getZkChildDeleteListener(clientName);

        iZkChildListener.handleChildChange(clientName, clientNameList);

        verify(clientService, times(1)).buildAndSendDeletedMessage(clientNameList, clientName);

    }

    @Test
    void getZkChildDeleteListenerThrowException() throws Exception {

        String clientName = "clientName";
        List<String> clientNameList = Arrays.asList(clientName);


        doThrow(IllegalStateException.class).when(clientService).buildAndSendDeletedMessage(anyList(),anyString());

        IZkChildListener iZkChildListener = clientService.getZkChildDeleteListener(clientName);

        iZkChildListener.handleChildChange(clientName,clientNameList);

        verify(clientService, times(1)).logError(String.format(NODE_ILLEGAL_STATUS_DELETING, clientName));

    }

    @Test
    void getZkChildUpdateListener() throws Exception {

        String clientName = "clientName";
        List<String> clientNameList = Arrays.asList(clientName);

        IZkChildListener iZkChildListener = clientService.getZkChildUpdateListener(clientName);

        iZkChildListener.handleChildChange(clientName, clientNameList);

        verify(clientService, times(1)).buildAndSendUpdatedMessage(clientName,EVENTS.ZK_STRATEGIES_EVENT_RECEIVED);
        verify(clientService, times(1)).logError(String.format(NODE_ILLEGAL_STATUS_DELETING, clientName));

    }

    @Test
    void getZkChildUpdateListenerThrowException() throws Exception {

        String clientName = "clientName";
        List<String> clientNameList = Arrays.asList(clientName);


        doThrow(IllegalStateException.class).when(clientService).buildAndSendUpdatedMessage(anyString(),any(EVENTS.class));

        IZkChildListener iZkChildListener = clientService.getZkChildUpdateListener(clientName);

        iZkChildListener.handleChildChange(clientName,clientNameList);

        verify(clientService, times(1)).buildAndSendUpdatedMessage(clientName,EVENTS.ZK_STRATEGIES_EVENT_RECEIVED);
        verify(clientService, times(1)).logError(String.format(NODE_ILLEGAL_STATUS_DELETING, clientName));

    }



/*
    @Test
    void getStrategies(String clientName) {
        return getAllChildren(String.format(CLIENT_STRATEGIES, clientName));
    }

    @Test
    void findAllClient() {
        return getAllChildren(CLIENTS);
    }

    @Test
    void addToLiveNodes(String nodeName) {
        zkClient.create(nodeName, "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }
*/

}
