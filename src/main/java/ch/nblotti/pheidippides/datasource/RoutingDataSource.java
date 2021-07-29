package ch.nblotti.pheidippides.datasource;

import ch.nblotti.pheidippides.client.ClientTO;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {


    public static final String DB_MIGRATION_LOCATION = "db/migration";
    public static final String NEW_CLIENT = "newClient";

    private final EnumMap<DataSourceEnum, DataSource> dataSources = new EnumMap<>(DataSourceEnum.class);
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

        return dataSources.get(lookupKey);
    }

    public void createDataSource(ClientTO clientTO) {
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
            dataSource.setUrl(clientTO.getDbUrl());
            dataSource.setUsername(clientTO.getDbUser());
            dataSource.setPassword(clientTO.getDbPassword());

            Flyway flyway = Flyway.configure()
                    .dataSource(clientTO.getDbUrl(), clientTO.getDbUser(), clientTO.getDbPassword())
                    .locations(DB_MIGRATION_LOCATION)
                    .load();
            flyway.migrate();
            dataSources.put(DataSourceEnum.CUSTOM, dataSource);


            message = MessageBuilder
                    .withPayload(EVENTS.SUCCESS)
                    .setHeader(NEW_CLIENT, clientTO)
                    .build();
        } catch (Exception ex) {
            message = MessageBuilder
                    .withPayload(EVENTS.ERROR)
                    .build();
        }
        stateMachine.sendEvent(message);


    }

    public void closeAllConnection() {

        for (Map.Entry<DataSourceEnum, DataSource> entry : dataSources.entrySet()) {
            DataSourceEnum key = entry.getKey();

            if (!key.equals(DataSourceEnum.HSQL)) {
                DataSource current = this.dataSources.get(key);
                this.dataSources.remove(key);
                closeConnection(current);
            }

        }
    }

    private void closeConnection(DataSource ds) {

        try {
            ds.getConnection().close();
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
        }

    }

    @Override
    public void afterPropertiesSet() {
        //Required, leave empty
    }
}


