package ch.nblotti.Pheidippides.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.nblotti.Pheidippides.zookeeper.ZooKeeperService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ZooKeeperServiceTest {


  ZooKeeperService zooKeeperService;
  ZkClient zkClient;
  DateTimeFormatter formatMessage;

  @BeforeEach
  void beforeEach() {
    zkClient = Mockito.mock(ZkClient.class);
    formatMessage = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    zooKeeperService = Mockito.spy(new ZooKeeperService(zkClient, formatMessage));
  }


  @Test
  void subscribe() {

    ClientListener listener = mock(ClientListener.class);
    // get the first free client
    String clientName = "test";

    doReturn(clientName).when(zooKeeperService).selectFreeClient();

    // register as a client listener in Zookeeper
    doNothing().when(zooKeeperService).registerToClientChanges(listener, clientName);


    // read client database info and send a message to subscribers
    doNothing().when(zooKeeperService).readDBInfoAndSendDBChangeInfo(clientName);

    // read client strategy related info and send a message to subscribers
    doNothing().when(zooKeeperService).chooseStrategyAndSendStrategyChangeEvent(clientName);

    zooKeeperService.subscribe(listener);

    verify(zooKeeperService, times(1)).selectFreeClient();
    verify(zooKeeperService, times(1)).registerToClientChanges(listener, clientName);
    verify(zooKeeperService, times(1)).readDBInfoAndSendDBChangeInfo(clientName);
    verify(zooKeeperService, times(1)).chooseStrategyAndSendStrategyChangeEvent(clientName);


  }

  @Test
  void registerToClientChanges() {

    String clientName = "test";
    ClientListener listener = mock(ClientListener.class);


    doNothing().when(zooKeeperService).addToClientLiveNodes(clientName);
    doNothing().when(zooKeeperService).registerToStrategyChanges(clientName);
    doNothing().when(zooKeeperService).registerTODBInfoChangeEvent(clientName);


    zooKeeperService.registerToClientChanges(listener, clientName);

    verify(zooKeeperService, times(1)).addToClientLiveNodes(clientName);
    verify(zooKeeperService, times(1)).registerToStrategyChanges(clientName);
    verify(zooKeeperService, times(1)).registerTODBInfoChangeEvent(clientName);

  }


  @Test
  void findAllClientNoCLient() {


    List<String> clients = new ArrayList<>();

    doReturn(clients).when(zooKeeperService).findAllClient();


    String returned = zooKeeperService.selectFreeClient();

    Assert.assertNull(returned);

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
    doReturn(clients).when(zooKeeperService).findAllClient();

    doReturn(returnedNotFreeClient).when(zooKeeperService).getLiveNodes(firstClient);
    doReturn(returnedNotFreeClient).when(zooKeeperService).getLiveNodes(secondClient);
    doReturn(returnedFreeClient).when(zooKeeperService).getLiveNodes(thirdClient);

    String returned = zooKeeperService.selectFreeClient();

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
    when(returnedNotFreeClient.size()).thenReturn(5);
    doReturn(clients).when(zooKeeperService).findAllClient();

    doReturn(returnedNotFreeClient).when(zooKeeperService).getLiveNodes(firstClient);
    doReturn(returnedNotFreeClient).when(zooKeeperService).getLiveNodes(secondClient);
    doReturn(returnedNotFreeClient).when(zooKeeperService).getLiveNodes(thirdClient);
    doReturn(returnedNotFreeClient).when(zooKeeperService).getLiveNodes(fourthClient);


    doReturn(1).when(zooKeeperService).getLiveNodesCount(firstClient);
    doReturn(1).when(zooKeeperService).getNodeAllowed(firstClient);
    doReturn(1).when(zooKeeperService).getLiveNodesCount(secondClient);
    doReturn(1).when(zooKeeperService).getNodeAllowed(secondClient);
    doReturn(1).when(zooKeeperService).getLiveNodesCount(thirdClient);
    doReturn(1).when(zooKeeperService).getNodeAllowed(thirdClient);
    doReturn(1).when(zooKeeperService).getLiveNodesCount(fourthClient);
    doReturn(2).when(zooKeeperService).getNodeAllowed(fourthClient);

    String returned = zooKeeperService.selectFreeClient();

    Assert.assertEquals(fourthClient, returned);

  }


  @Test
  void registerNode() {

    String clientStr = "first";
    String UUID = "UUID";
    ClientListener listener = mock(ClientListener.class);


    doReturn(UUID).when(zooKeeperService).getUuid();

    String path = String.format(CLIENT_LIVE_NODES + "/%s", clientStr, UUID);

    doNothing().when(zooKeeperService).addToLiveNodes(path);
    zooKeeperService.addToClientLiveNodes(clientStr);

    verify(zooKeeperService, times(1)).addToLiveNodes(path);

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

    doReturn(liveNodes).when(zooKeeperService).getLiveNodes(anyString());

    int returned = zooKeeperService.getLiveNodesIndex(firstEntry);

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

    doReturn(liveNodes).when(zooKeeperService).getAllChildren(anyString());
    doReturn(secondEntry).when(zooKeeperService).getUuid();
    int returned = zooKeeperService.getLiveNodesIndex(secondEntry);

    assertEquals(2, returned);
  }

  @Test
  void getAllNodesChildren() {
    String pathStr = "/current/path";
    List<String> returnStr = Arrays.asList("1", "2", "3", "4");

    doReturn(Boolean.TRUE).when(zkClient).exists(pathStr);
    doReturn(returnStr).when(zkClient).getChildren(pathStr);

    List<String> result = zooKeeperService.getAllChildren(pathStr);

    Assert.assertEquals(result, returnStr);

  }

  @Test
  void getAllNodesChildrenNoPath() {
    String pathStr = "/current/path";
    List<String> returnStr = Arrays.asList("1", "2", "3", "4");

    doReturn(Boolean.FALSE).when(zkClient).exists(pathStr);

    Exception exception = assertThrows(IllegalStateException.class, () -> {
      List<String> result = zooKeeperService.getAllChildren(pathStr);
    });

    assertTrue(exception.getMessage().contains("No node /allNodes exists"));


  }


  @Test
  public void readNodeData() {

    String path = "path";

    doReturn(path).when(zkClient).readData(path, true);

    String result = zooKeeperService.readNodeData(path);

    verify(zkClient, times(1)).readData(path, true);

  }


  @Test
  public void registerChildrenChangeWatcher() {
    List<String> toReturn = mock(List.class);
    String clientStr = "client";
    IZkChildListener iZkChildListener = mock(IZkChildListener.class);

    doReturn(toReturn).when(zkClient).subscribeChildChanges(anyString(), any(IZkChildListener.class));

    zooKeeperService.registerToStrategyChanges(clientStr);

    verify(zkClient, times(3)).subscribeChildChanges(anyString(), any(IZkChildListener.class));


  }

  @Test
  public void closeConnection() {
    zooKeeperService.closeConnection();

    verify(zkClient, times(1)).close();

  }

  @Test
  public void readDBInfoAndSendDBChangeInfo() {
    String clientName = "test";
    String dbUrlStr = String.format(CLIENT_DB_URL, clientName);
    String dbUrl = "jdbc://";
    String dbUserStr = String.format(CLIENT_DB_USER, clientName);
    String dbUser = "testuser";
    String dbPasswordStr = String.format(CLIENT_DB_PASSWORD, clientName);
    String dbPassword = "testpassword";

    ClientListener listener1 = mock(ClientListener.class);
    ClientListener listener2 = mock(ClientListener.class);
    ClientListener listener3 = mock(ClientListener.class);
    List<ClientListener> listeners = new ArrayList<>();

    listeners.add(listener1);
    listeners.add(listener2);
    listeners.add(listener3);

    doReturn(dbUrl).when(zooKeeperService).readNodeData(dbUrlStr);
    doReturn(dbUser).when(zooKeeperService).readNodeData(dbUserStr);
    doReturn(dbPassword).when(zooKeeperService).readNodeData(dbPasswordStr);
    doReturn(listeners).when(zooKeeperService).getListeners();

    zooKeeperService.readDBInfoAndSendDBChangeInfo(clientName);


    verify(listener1, times(1)).handleDbInfoChange(dbUrl, dbUser, dbPassword);
    verify(listener2, times(1)).handleDbInfoChange(dbUrl, dbUser, dbPassword);
    verify(listener2, times(1)).handleDbInfoChange(dbUrl, dbUser, dbPassword);

  }

  @Test
  void chooseStrategyAndSendStrategyChangeEvent() {

    String clientName = "test";
    List<StrategiesDTO> followedRange = mock(List.class);

    ClientListener listener1 = mock(ClientListener.class);
    ClientListener listener2 = mock(ClientListener.class);
    ClientListener listener3 = mock(ClientListener.class);
    List<ClientListener> listeners = new ArrayList<>();

    listeners.add(listener1);
    listeners.add(listener2);
    listeners.add(listener3);

    doReturn(followedRange).when(zooKeeperService).chooseStrategy(clientName);
    doReturn(listeners).when(zooKeeperService).getListeners();

    zooKeeperService.chooseStrategyAndSendStrategyChangeEvent(clientName);

    verify(listener1, times(1)).handleStrategyChange(followedRange);
    verify(listener2, times(1)).handleStrategyChange(followedRange);
    verify(listener2, times(1)).handleStrategyChange(followedRange);

  }


  @Test
  void chooseStrategyNoStrategyForClient() {

    String clientName = "test";
    int position = 1;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 0;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 1;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 0;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);


    Exception exception = assertThrows(IllegalStateException.class, () -> {
      zooKeeperService.chooseStrategy(clientName);
    });
  }

  @Test
  void chooseStrategyOneNodeTwoStrategy() {

    String clientName = "test";
    int position = 1;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 0;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 1;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 2;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);


    List<String> strategies = new ArrayList<>();
    String firstStrategy = "firstStrategy";
    String secondStrategy = "secondStrategy";

    strategies.add(firstStrategy);
    strategies.add(secondStrategy);

    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(returned.size(), 2);
    assertTrue(firstStrategy.equals(returned.get(0).getStrategyName()));
    assertTrue(secondStrategy.equals(returned.get(1).getStrategyName()));
  }


  @Test
  void chooseTwoStrategyTwoNodeFirstPosition() {


    String clientName = "test";
    int position = 1;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 2;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 2;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 2;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);


    List<String> strategies = new ArrayList<>();
    String firstStrategy = "firstStrategy";
    String secondStrategy = "secondStrategy";

    strategies.add(firstStrategy);
    strategies.add(secondStrategy);

    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(1, returned.size());
    assertTrue(firstStrategy.equals(returned.get(0).getStrategyName()));

  }

  @Test
  void chooseTwoStrategyTwoNodeSecondPosition() {

    String clientName = "test";
    int position = 2;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 2;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 2;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 2;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);


    List<String> strategies = new ArrayList<>();
    String firstStrategy = "firstStrategy";
    String secondStrategy = "secondStrategy";

    strategies.add(firstStrategy);
    strategies.add(secondStrategy);

    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(1, returned.size());
    assertTrue(secondStrategy.equals(returned.get(0).getStrategyName()));
  }


  @Test
  void chooseThreeStrategyTwoNodeFirstPosition() {

    String clientName = "test";
    int position = 1;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 2;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 2;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 3;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);


    List<String> strategies = new ArrayList<>();
    String firstStrategy = "firstStrategy";
    String secondStrategy = "secondStrategy";
    String thirdStrategy = "thirdStrategy";

    strategies.add(firstStrategy);
    strategies.add(secondStrategy);
    strategies.add(thirdStrategy);

    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(1, returned.size());
    assertTrue(firstStrategy.equals(returned.get(0).getStrategyName()));
  }


  @Test
  void chooseThreeStrategyTwoNodeLastPosition() {

    String clientName = "test";
    int position = 2;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 2;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 2;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 3;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);


    List<String> strategies = new ArrayList<>();
    String firstStrategy = "firstStrategy";
    String secondStrategy = "secondStrategy";
    String thirdStrategy = "thirdStrategy";

    strategies.add(firstStrategy);
    strategies.add(secondStrategy);
    strategies.add(thirdStrategy);

    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(2, returned.size());
    assertTrue(secondStrategy.equals(returned.get(0).getStrategyName()));
    assertTrue(thirdStrategy.equals(returned.get(1).getStrategyName()));
  }

  @Test
  void chooseSevenStrategyThreeNodeLastPosition() {


    String clientName = "test";
    int position = 3;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 3;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 3;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 7;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);

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


    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(3, returned.size());
    assertTrue(fifthtrategy.equals(returned.get(0).getStrategyName()));
    assertTrue(sixthStrategy.equals(returned.get(1).getStrategyName()));
    assertTrue(seventhStrategy.equals(returned.get(2).getStrategyName()));


  }

  @Test
  void chooseSevenStrategyThreeNodeSecondPosition() {

    String clientName = "test";
    int position = 2;
    doReturn(position).when(zooKeeperService).getLiveNodesIndex(clientName);
    int liveNodeCount = 3;
    doReturn(liveNodeCount).when(zooKeeperService).getLiveNodesCount(clientName);

    int allowedNode = 3;
    doReturn(allowedNode).when(zooKeeperService).getNodeAllowed(clientName);

    int strategiesCount = 7;
    doReturn(strategiesCount).when(zooKeeperService).getStrategiesCount(clientName);

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


    doReturn(strategies).when(zooKeeperService).getStrategies(clientName);


    List<StrategiesDTO> returned = zooKeeperService.chooseStrategy(clientName);

    assertEquals(2, returned.size());
    assertTrue(thirdStrategy.equals(returned.get(0).getStrategyName()));
    assertTrue(fourthtrategy.equals(returned.get(1).getStrategyName()));
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

    doReturn(liveNodes).when(zooKeeperService).getLiveNodes(clientName);
    doReturn(secondEntry).when(zooKeeperService).getUuid();

    int index = zooKeeperService.getLiveNodesIndex(clientName);

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

    doReturn(liveNodes).when(zooKeeperService).getLiveNodes(clientName);
    doReturn(fourthEntry).when(zooKeeperService).getUuid();

    int index = zooKeeperService.getLiveNodesIndex(clientName);

    assertEquals(-1, index);
  }

  @Test
  void getNodeAllowed() {
    String clientName = "clientName";
    String path = String.format(CLIENT_NODE_ALLOWED, clientName);

    doReturn("3").when(zooKeeperService).readNodeData(path);

    int returned = zooKeeperService.getNodeAllowed(clientName);
    Assert.assertEquals(3, returned);


  }

  @Test
  void getNodeAllowedException() {

    String clientName = "clientName";
    String path = String.format(CLIENT_NODE_ALLOWED, clientName);

    doReturn("").when(zooKeeperService).readNodeData(path);


      int returned = zooKeeperService.getNodeAllowed(clientName);

    Assert.assertEquals(0, returned);
  }

}


