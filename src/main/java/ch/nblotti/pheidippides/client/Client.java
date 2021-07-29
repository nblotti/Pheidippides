package ch.nblotti.pheidippides.client;

import java.util.List;

public class Client {


    private final String userName;

    private final ClientDBInfo clientDBInfo;

    private final List<StrategiesDTO> strategies;

    public Client(String userName, ClientDBInfo clientDBInfo, List<StrategiesDTO> strategies) {
        this.userName = userName;
        this.clientDBInfo = clientDBInfo;
        this.strategies = strategies;
    }

    public List<StrategiesDTO> getStrategies() {
        return strategies;
    }

    public String getUserName() {
        return userName;
    }

    public String getDbUrl() {
        return clientDBInfo.getDbUrl();
    }

    public String getDbUser() {
        return clientDBInfo.getDbUser();
    }

    public String getDbPassword() {
        return clientDBInfo.getDbPassword();
    }


}



