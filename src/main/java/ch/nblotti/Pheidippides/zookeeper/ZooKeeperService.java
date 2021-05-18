package ch.nblotti.Pheidippides.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.net.http.WebSocket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Repository
@Slf4j
@Validated
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ZooKeeperService {


  public static final String CLIENTS = "/rstrategy/clients";
  public static final String CLIENT_LIVE_NODES = CLIENTS + "/%s/liveNodes";
  public static final String CLIENT_NODE_ALLOWED = CLIENTS + "/%s/nodeAllowed";

  public static final String CLIENT_DB_URL = CLIENTS + "/%s/db/dbUrl";
  public static final String CLIENT_DB_USER = CLIENTS + "/%s/db/dbUser";
  public static final String CLIENT_DB_PASSWORD = CLIENTS + "/%s/db/dbPassword";

  public static final String CLIENT_STRATEGIES = CLIENTS + "/%s/strategies";

  private final ZkClient zkClient;

  private final String UUID;
  private final DateTimeFormatter formatMessage;


  private final List<ClientListener> listeners = new ArrayList<>();


  @Autowired
  public ZooKeeperService(ZkClient zkClient, DateTimeFormatter formatMessage) {

    this.zkClient = zkClient;
    this.UUID = java.util.UUID.randomUUID().toString();
    this.formatMessage = formatMessage;
  }

  public void subscribe(ClientListener listener) {

    // get the first free client
    String clientName = selectFreeClient();

    // register as a client listener in Zookeeper
    registerToClientChanges(listener, clientName);

    // read client database info and send a message to subscribers
    readDBInfoAndSendDBChangeInfo(clientName);

    // read client strategy related info and send a message to subscribers
    chooseStrategyAndSendStrategyChangeEvent(clientName);

  }

  void registerToClientChanges(ClientListener listener, String clientName) {
    addToClientLiveNodes(clientName);
    registerToStrategyChanges(clientName);
    registerTODBInfoChangeEvent(clientName);
    this.listeners.add(listener);
  }


  String selectFreeClient() {


    List<String> freeClients = findAllClient();
    for (String clientName : freeClients) {
      if (getLiveNodes(clientName).size() == 0) {
        return clientName;
      }
    }
    for (String clientName : freeClients) {
      if (getLiveNodesCount(clientName) < getNodeAllowed(clientName)) {
        return clientName;
      }
    }
    return null;

  }

  void addToClientLiveNodes(String clientName) {

    String path = String.format(CLIENT_LIVE_NODES + "/%s", clientName, getUuid());
    addToLiveNodes(path);
  }

  String getUuid() {
    return this.UUID;
  }


  void registerToStrategyChanges(String clientName) {
    String liveNodesPath = String.format(CLIENT_LIVE_NODES, clientName);
    String strategiesPath = String.format(CLIENT_STRATEGIES, clientName);
    String nodeAllowedPath = String.format(CLIENT_NODE_ALLOWED, clientName);


    List<String> allowedNodes = zkClient.subscribeChildChanges(nodeAllowedPath, new IZkChildListener() {
      @Override
      public void handleChildChange(String parentPath, List<String> list) throws Exception {
        String[] clients = parentPath.split("/");
        chooseStrategyAndSendStrategyChangeEvent(clients[clients.length - 2]);
      }
    });

    List<String> liveNodes = zkClient.subscribeChildChanges(liveNodesPath, new IZkChildListener() {
      @Override
      public void handleChildChange(String parentPath, List<String> list) throws Exception {
        String[] clients = parentPath.split("/");
        chooseStrategyAndSendStrategyChangeEvent(clients[clients.length - 2]);
      }
    });
    List<String> strategies = zkClient.subscribeChildChanges(strategiesPath, new IZkChildListener() {
      @Override
      public void handleChildChange(String parentPath, List<String> list) throws Exception {
        String[] clients = parentPath.split("/");
        chooseStrategyAndSendStrategyChangeEvent(clients[clients.length - 2]);

      }
    });


  }

  void registerTODBInfoChangeEvent(String clientName) {
    String dbNodesPath = String.format(CLIENT_DB_URL, clientName);


    List<String> liveNodes = zkClient.subscribeChildChanges(dbNodesPath, new IZkChildListener() {
      @Override
      public void handleChildChange(String parentPath, List<String> list) throws Exception {
        String[] clients = parentPath.split("/");
        readDBInfoAndSendDBChangeInfo(clients[clients.length - 2]);
      }
    });


  }

  public void readDBInfoAndSendDBChangeInfo(String clientName) {

    String dbUrl = readNodeData(String.format(CLIENT_DB_URL, clientName));
    String dbUser = readNodeData(String.format(CLIENT_DB_USER, clientName));
    String dbPassword = readNodeData(String.format(CLIENT_DB_PASSWORD, clientName));
    getListeners().stream().forEach(s -> s.handleDbInfoChange(dbUrl, dbUser, dbPassword));
  }

  List<ClientListener> getListeners() {
    return this.listeners;
  }

  void chooseStrategyAndSendStrategyChangeEvent(String clientName) {
    List<StrategiesDTO> followedRange = chooseStrategy(clientName);
    getListeners().stream().forEach(s -> s.handleStrategyChange(followedRange));
  }

  List<StrategiesDTO> chooseStrategy(String clientName) {


    int position = getLiveNodesIndex(clientName);
    int liveNodeCount = getLiveNodesCount(clientName);
    int allowedNode = getNodeAllowed(clientName);
    int strategiesCount = getStrategiesCount(clientName);
    int min;
    int max;

    if (position == -1 || strategiesCount == 0)
      throw new IllegalStateException();


    if (allowedNode == 1 || liveNodeCount == 1) {
      min = 1;
      max = strategiesCount;
    } else {

      //on calcule la fenetre par défault
      int windows = strategiesCount / allowedNode;
      min = ((position - 1) * windows) + 1;
      max = position * windows;

      //le nombre de strategy ne se divise pas par le nombre de runner, le dernier va devoir prendre le delta.
      int reste = strategiesCount % allowedNode;

      if (reste != 0) {

        //est on le dernier ?
        if (position == (strategiesCount - reste) / windows) {
          min = ((position - 1) * windows) + 1;
          max = strategiesCount;
        }
      }
    }
    List<StrategiesDTO> strategies = new ArrayList<>();

    int i = 1;
    for (Iterator<String> ie = getStrategies(clientName).iterator(); ie.hasNext(); ) {
      String strategyName = ie.next();
      if (i >= min && i <= max)
        strategies.add(new StrategiesDTO(clientName, strategyName));
      i++;
    }
    return strategies;
  }


  int getNodeAllowed(String clientName) {
    try {
      String nodeAllowedStr = readNodeData(String.format(CLIENT_NODE_ALLOWED, clientName));
      return Integer.parseInt(nodeAllowedStr);
    } catch (NumberFormatException ex) {
      return 0;
    }

  }

   int getLiveNodesCount(String clientName) {
    return getLiveNodes(clientName).size();
  }

   List<String> getLiveNodes(String clientName) {
    return getAllChildren(String.format(CLIENT_LIVE_NODES, clientName));
  }

   int getStrategiesCount(String clientName) {
    return getStrategies(clientName).size();
  }

   List<String> getStrategies(String clientName) {
    return getAllChildren(String.format(CLIENT_STRATEGIES, clientName));
  }


   List<String> findAllClient() {
    return getAllChildren(CLIENTS);
  }


   void addToLiveNodes(String nodeName) {

    String path = zkClient.create(nodeName, "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

  }


   int getLiveNodesIndex(String clientName) {
    int result = 0;
    for (String entry : getLiveNodes(clientName)) {
      if (entry.equals(getUuid())) return ++result;
      result++;
    }
    return -1;
  }


  List<String> getAllChildren(String id) {
    if (!zkClient.exists(id)) {
      throw new IllegalStateException("No node /allNodes exists");
    }
    return zkClient.getChildren(id);
  }

   String readNodeData(String path) {
    return zkClient.readData(path, true);
  }


   void closeConnection() {
    zkClient.close();
  }


}
