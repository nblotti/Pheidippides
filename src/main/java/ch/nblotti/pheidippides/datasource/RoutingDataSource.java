package ch.nblotti.pheidippides.datasource;

import ch.nblotti.pheidippides.client.Client;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
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


        if (getDataSources().containsKey(DataSourceEnum.CUSTOM))
            return DataSourceEnum.CUSTOM;
        else if (getDataSources().containsKey(DataSourceEnum.HSQL))
            return DataSourceEnum.HSQL;

        throw new IllegalStateException("No dataSource defined");
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSourceEnum lookupKey = (DataSourceEnum) determineCurrentLookupKey();

        return getDataSources().get(lookupKey);
    }

    public void createDataSource(Client client) {
        Message<EVENTS> message;

        try {
            if (getDataSources().containsKey(DataSourceEnum.CUSTOM)) {
                closeConnection(getDataSources().get(DataSourceEnum.CUSTOM));
                dataSources.remove(DataSourceEnum.CUSTOM);
            }
            if (getDataSources().containsKey(DataSourceEnum.HSQL)) {
                closeConnection(getDataSources().get(DataSourceEnum.HSQL));
                dataSources.remove(DataSourceEnum.HSQL);
            }
            DriverManagerDataSource dataSource = doCreateDataSourceFromClient(client);

            doMigration(client, dataSource);


            message = MessageBuilder
                    .withPayload(EVENTS.SUCCESS)
                    .setHeader(NEW_CLIENT, client)
                    .build();
        } catch (Exception ex) {
            message = MessageBuilder
                    .withPayload(EVENTS.ERROR)
                    .build();
        }
        stateMachine.sendEvent(message);


    }

    @NotNull
    EnumMap<DataSourceEnum, DataSource> getDataSources() {
        return dataSources;
    }

    boolean doMigration(Client client, DriverManagerDataSource dataSource) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(client.getDbUrl(), client.getDbUser(), client.getDbPassword())
                    .locations(DB_MIGRATION_LOCATION)
                    .load();
            flyway.migrate();
            getDataSources().put(DataSourceEnum.CUSTOM, dataSource);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
        return true;
    }

    @NotNull
    DriverManagerDataSource doCreateDataSourceFromClient(Client client) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        if (client.getDbUrl() == null || client.getDbUrl().isEmpty())
            throw new IllegalStateException("Db url can't be null");

        if (client.getDbUser() == null || client.getDbUser().isEmpty())
            throw new IllegalStateException("UserName can't be null");

        if (client.getDbPassword() == null || client.getDbPassword().isEmpty())
            throw new IllegalStateException("Password can't be null");

        dataSource.setUrl(client.getDbUrl());
        dataSource.setUsername(client.getDbUser());
        dataSource.setPassword(client.getDbPassword());
        return dataSource;
    }

    public void closeAllConnection() {

        for (Map.Entry<DataSourceEnum, DataSource> entry : getDataSources().entrySet()) {
            DataSourceEnum key = entry.getKey();

            if (!key.equals(DataSourceEnum.HSQL)) {
                DataSource current = this.getDataSources().get(key);
                this.getDataSources().remove(key);
                closeConnection(current);
            }

        }
    }

    boolean closeConnection(DataSource ds) {

        try {
            ds.getConnection().close();
            return true;
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
            return false;
        }

    }

    @Override
    public void afterPropertiesSet() {
        //Required, leave empty
    }
}


