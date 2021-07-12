package ch.nblotti.pheidippides.datasource;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class RoutingDataSource extends AbstractRoutingDataSource {


    public static final String DB_MIGRATION_LOCATION = "db/migration";
    public static final String NEW_CLIENT = "newClient";

    private final Map<DataSourceEnum, DataSource> dataSources = new HashMap<>();
    private final StateMachine<STATES, EVENTS> stateMachine;

    public RoutingDataSource(DataSource dataSource, StateMachine<STATES, EVENTS> stateMachine) {
        dataSources.put(DataSourceEnum.HSQL, dataSource);
        this.stateMachine = stateMachine;
    }


    @Override
    protected Object determineCurrentLookupKey() {


        if (dataSources.containsKey(DataSourceEnum.CUSTOM))
            return DataSourceEnum.CUSTOM;
        else if (dataSources.containsKey(DataSourceEnum.HSQL))
            return DataSourceEnum.HSQL;

        throw new IllegalStateException("No dataSource defined");
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSourceEnum lookupKey = (DataSourceEnum) determineCurrentLookupKey();

        return (DataSource) dataSources.get(lookupKey);
    }

    public void createDataSource(ClientDTO clientDTO) {
        Message<EVENTS> message;

        try {
            if (dataSources.containsKey(DataSourceEnum.CUSTOM)) {
                closeConnection(dataSources.get(DataSourceEnum.CUSTOM));
                dataSources.remove(DataSourceEnum.CUSTOM);
            }
            if (dataSources.containsKey(DataSourceEnum.HSQL)) {
                closeConnection(dataSources.get(DataSourceEnum.HSQL));
                dataSources.remove(DataSourceEnum.HSQL);
            }
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(clientDTO.getDbUrl());
            dataSource.setUsername(clientDTO.getDbUser());
            dataSource.setPassword(clientDTO.getDbPassword());

            Flyway flyway = Flyway.configure()
                    .dataSource(clientDTO.getDbUrl(), clientDTO.getDbUser(), clientDTO.getDbPassword())
                    .locations(DB_MIGRATION_LOCATION)
                    .load();
            flyway.migrate();
            dataSources.put(DataSourceEnum.CUSTOM, dataSource);


            message = MessageBuilder
                    .withPayload(EVENTS.SUCCESS)
                    .setHeader(NEW_CLIENT, clientDTO)
                    .build();
        } catch (Exception ex) {
            message = MessageBuilder
                    .withPayload(EVENTS.ERROR)
                    .build();
        }
        stateMachine.sendEvent(message);


    }

    private void closeConnection(DataSource ds) {

        try {
            ds.getConnection().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public void afterPropertiesSet() {

    }
}


