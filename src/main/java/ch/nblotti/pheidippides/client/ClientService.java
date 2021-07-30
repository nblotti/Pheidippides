package ch.nblotti.pheidippides.client;

import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
@Slf4j
@Validated
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientService {


    public static final String CLIENTS = "/rstrategy/clients";
    public static final String CLIENT_LIVE_NODES = CLIENTS + "/%s/liveNodes";
    public static final String CLIENT_NODE_ALLOWED = CLIENTS + "/%s/nodeAllowed";

    public static final String CLIENT_DB_URL = CLIENTS + "/%s/db/dbUrl";
    public static final String CLIENT_DB_USER = CLIENTS + "/%s/db/dbUser";
    public static final String CLIENT_DB_PASSWORD = CLIENTS + "/%s/db/dbPassword";

    public static final String CLIENT_STRATEGIES = CLIENTS + "/%s/strategies";
    public static final String NEW_CLIENT = "newClient";
    public static final String FOLLOWED_CLIENT = "followedClient";
    public static final String NODE_ILLEGAL_STATUS_DELETING = "node %s in illegal status, deleting";

    private final ZkClient zkClient;

    private final String uuid;

    private StateMachine<STATES, EVENTS> stateMachine;


    @Autowired
    public ClientService(ZkClient zkClient, StateMachine<STATES, EVENTS> stateMachine) {

        this.zkClient = zkClient;

        this.stateMachine = stateMachine;
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public void subscribe() {


        // get the first free client
        String clientName = selectFreeClient();
        if (clientName == null) {
            stateMachine.sendEvent(EVENTS.WAIT_FOR_CLIENT);
            return;
        }

        // register as a client listener in Zookeeper
        registerToClientChanges(clientName);

        buildAndSendUpdatedMessage(clientName, EVENTS.SUCCESS);


    }

    public void unSubscribe(Client client) {
        zkClient.unsubscribeAll();
        this.removeFromClientLiveNodes(client.getUserName());

    }


    void buildAndSendUpdatedMessage(String clientName, EVENTS events) {

        // read client database info and send a message to subscribers
        ClientDBInfo clientDBinfo = readDBInfo(clientName);

        // read client strategy related info and send a message to subscribers
        List<StrategiesDTO> strategies = chooseStrategy(clientName);

        Client client = new Client(clientName, clientDBinfo, strategies);

        Message<EVENTS> message = MessageBuilder
                .withPayload(events)
                .setHeader(NEW_CLIENT, client)
                .build();

        stateMachine.sendEvent(message);


    }


    void registerToClientChanges(String clientName) {
        addToClientLiveNodes(clientName);
        registerToStrategyChanges(clientName);
        registerTODBInfoChangeEvent(clientName);
    }


    String selectFreeClient() {


        List<String> freeClients = findAllClient();
        for (String clientName : freeClients) {
            if (getNodeAllowed(clientName) != 0 && getLiveNodes(clientName).isEmpty()) {
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

    boolean removeFromClientLiveNodes(String clientName) {

        String path = String.format(CLIENT_LIVE_NODES + "/%s", clientName, getUuid());
        return removeFromLiveNodes(path);
    }

    String getUuid() {
        return this.uuid;
    }


    void registerToStrategyChanges(String clientName) {
        String liveNodesPath = String.format(CLIENT_LIVE_NODES, clientName);
        String strategiesPath = String.format(CLIENT_STRATEGIES, clientName);
        String nodeAllowedPath = String.format(CLIENT_NODE_ALLOWED, clientName);


        zkClient.subscribeChildChanges(CLIENTS, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> list) throws Exception {

                buildAndSendDeletedMessage(list, clientName);

            }
        });

        zkClient.subscribeDataChanges(nodeAllowedPath, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {
                try {
                    Message<EVENTS> message = MessageBuilder
                            .withPayload(EVENTS.ZK_CLIENT_CHANGE_EVENT_RECEIVED)
                            .setHeader(FOLLOWED_CLIENT, true)
                            .build();
                    stateMachine.sendEvent(message);

                } catch (IllegalStateException ex) {
                    log.error(String.format(NODE_ILLEGAL_STATUS_DELETING, clientName));
                }
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                throw new UnsupportedOperationException();
            }

        });

        zkClient.subscribeChildChanges(liveNodesPath, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> list) throws Exception {
                try {
                    buildAndSendUpdatedMessage(clientName, EVENTS.ZK_STRATEGIES_EVENT_RECEIVED);
                } catch (IllegalStateException ex) {
                    log.error(String.format(NODE_ILLEGAL_STATUS_DELETING, clientName));
                }
            }
        });
        zkClient.subscribeChildChanges(strategiesPath, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> list) throws Exception {
                try {
                    buildAndSendUpdatedMessage(clientName, EVENTS.ZK_STRATEGIES_EVENT_RECEIVED);
                } catch (IllegalStateException ex) {
                    log.error(String.format(NODE_ILLEGAL_STATUS_DELETING, clientName));
                }

            }
        });


    }

     void buildAndSendDeletedMessage(List<String> list, String clientName) {
        boolean followedClientDeleted = false;

        if (!list.contains(clientName))
            followedClientDeleted = true;

        Message<EVENTS> message = MessageBuilder
                .withPayload(EVENTS.ZK_CLIENT_CHANGE_EVENT_RECEIVED)
                .setHeader(FOLLOWED_CLIENT, followedClientDeleted)
                .build();

        stateMachine.sendEvent(message);
    }

    void registerTODBInfoChangeEvent(String clientName) {
        String dbNodesPath = String.format(CLIENT_DB_URL, clientName);


        zkClient.subscribeDataChanges(dbNodesPath, new IZkDataListener() {

            @Override
            public void handleDataChange(String s, Object o) throws Exception {
                buildAndSendUpdatedMessage(clientName, EVENTS.ZK_DB_EVENT_RECEIVED);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                buildAndSendUpdatedMessage(clientName, EVENTS.ZK_DB_EVENT_RECEIVED);
            }

        });


    }

    public ClientDBInfo readDBInfo(String clientName) {

        String dbUrl = readNodeData(String.format(CLIENT_DB_URL, clientName));
        String dbUser = readNodeData(String.format(CLIENT_DB_USER, clientName));
        String dbPassword = readNodeData(String.format(CLIENT_DB_PASSWORD, clientName));

        return new ClientDBInfo(dbUrl, dbUser, dbPassword);
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

            //est on le dernier ?
            if (reste != 0 && windows != 0 && position == (strategiesCount - reste) / windows) {
                min = ((position - 1) * windows) + 1;
                max = strategiesCount;
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

        zkClient.create(nodeName, "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

    }

    boolean removeFromLiveNodes(String nodeName) {

        return zkClient.delete(nodeName);

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
