package ch.nblotti.pheidippides.datasource;

import ch.nblotti.pheidippides.client.Client;
import ch.nblotti.pheidippides.client.ClientService;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.I0Itec.zkclient.ZkClient;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingDataSourceTest {


    RoutingDataSource routingDataSource;

    @Mock
    DataSource hsqlDb;

    @Mock
    StateMachine<STATES, EVENTS> stateMachine;

    @BeforeEach
    void setUp() {

        routingDataSource = Mockito.spy(new RoutingDataSource(hsqlDb, stateMachine));

    }

    @Test
    void determineCurrentLookupKeyHSQLDB() {

        DataSource custom = mock(DataSource.class);
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);

        doReturn(dataSources).when(routingDataSource).getDataSources();
        when(dataSources.containsKey(DataSourceEnum.CUSTOM)).thenReturn(false);
        when(dataSources.containsKey(DataSourceEnum.HSQL)).thenReturn(true);


        Object returned = routingDataSource.determineCurrentLookupKey();

        Assert.assertEquals(DataSourceEnum.HSQL, returned);
    }

    @Test
    void determineCurrentLookupKeyCustom() {

        DataSource custom = mock(DataSource.class);
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);

        doReturn(dataSources).when(routingDataSource).getDataSources();
        when(dataSources.containsKey(DataSourceEnum.CUSTOM)).thenReturn(true);


        Object returned = routingDataSource.determineCurrentLookupKey();

        Assert.assertEquals(DataSourceEnum.CUSTOM, returned);
    }

    @Test
    void determineCurrentLookupKeyNoDataBase() {

        DataSource custom = mock(DataSource.class);
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);

        doReturn(dataSources).when(routingDataSource).getDataSources();
        when(dataSources.containsKey(DataSourceEnum.CUSTOM)).thenReturn(false);
        when(dataSources.containsKey(DataSourceEnum.HSQL)).thenReturn(false);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.determineCurrentLookupKey();
        });


    }


    @Test
    void createDataSourceWithNoCustom() {

        ArgumentCaptor<Message<EVENTS>> emailCaptor = ArgumentCaptor.forClass(Message.class);


        Client client = mock(Client.class);
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);

        doReturn(Boolean.TRUE).when(routingDataSource).closeConnection(hsqlDb);

        doReturn(dataSource).when(routingDataSource).doCreateDataSourceFromClient(client);

        doReturn(Boolean.TRUE).when(routingDataSource).doMigration(client, dataSource);

        routingDataSource.createDataSource(client);


        verify(stateMachine, times(1)).sendEvent(emailCaptor.capture());
        verify(routingDataSource, times(1)).doMigration(client, dataSource);
        verify(routingDataSource, times(1)).doCreateDataSourceFromClient(client);
        verify(routingDataSource, times(1)).closeConnection(hsqlDb);

        Message<EVENTS> message = emailCaptor.getValue();

        assertEquals(EVENTS.SUCCESS, message.getPayload());
    }

    @Test
    void createDataSourceWithCustom() {

        ArgumentCaptor<Message<EVENTS>> emailCaptor = ArgumentCaptor.forClass(Message.class);
        DataSource custom = mock(DataSource.class);
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);


        when(dataSources.containsKey(DataSourceEnum.CUSTOM)).thenReturn(true);
        when(dataSources.get(DataSourceEnum.CUSTOM)).thenReturn(custom);

        Client client = mock(Client.class);
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);

        doReturn(Boolean.TRUE).when(routingDataSource).closeConnection(custom);
        doReturn(dataSources).when(routingDataSource).getDataSources();
        doReturn(dataSource).when(routingDataSource).doCreateDataSourceFromClient(client);
        doReturn(Boolean.TRUE).when(routingDataSource).doMigration(client, dataSource);

        routingDataSource.createDataSource(client);


        verify(stateMachine, times(1)).sendEvent(emailCaptor.capture());
        verify(routingDataSource, times(1)).doMigration(client, dataSource);
        verify(routingDataSource, times(1)).doCreateDataSourceFromClient(client);
        verify(routingDataSource, times(1)).closeConnection(custom);
        verify(routingDataSource, times(0)).closeConnection(hsqlDb);

        Message<EVENTS> message = emailCaptor.getValue();

        assertEquals(EVENTS.SUCCESS, message.getPayload());
    }


    @Test
    void createDataSourceThrowError() {

        ArgumentCaptor<Message<EVENTS>> emailCaptor = ArgumentCaptor.forClass(Message.class);


        Client client = mock(Client.class);
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);

        doReturn(Boolean.TRUE).when(routingDataSource).closeConnection(hsqlDb);

        doThrow(IllegalStateException.class).when(routingDataSource).doCreateDataSourceFromClient(client);

        routingDataSource.createDataSource(client);


        verify(stateMachine, times(1)).sendEvent(emailCaptor.capture());
        verify(routingDataSource, times(0)).doMigration(client, dataSource);
        verify(routingDataSource, times(1)).doCreateDataSourceFromClient(client);
        verify(routingDataSource, times(1)).closeConnection(hsqlDb);

        Message<EVENTS> message = emailCaptor.getValue();

        assertEquals(EVENTS.ERROR, message.getPayload());
    }

    @Test
    void oCreateDataSourceFromClientNullOrEmptyDbUrl() {

        Client client = mock(Client.class);

        when(client.getDbUrl()).thenReturn(null);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.doCreateDataSourceFromClient(client);
        });

        when(client.getDbUrl()).thenReturn("");

        exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.doCreateDataSourceFromClient(client);
        });
    }

    @Test
    void oCreateDataSourceFromClientNullOrEmptyDbUserName() {

        Client client = mock(Client.class);

        when(client.getDbUrl()).thenReturn("test");
        when(client.getDbUser()).thenReturn(null);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.doCreateDataSourceFromClient(client);
        });

        when(client.getDbUser()).thenReturn("");

        exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.doCreateDataSourceFromClient(client);
        });

    }


    @Test
    void oCreateDataSourceFromClientNullOrEmptyPassword() {

        Client client = mock(Client.class);

        when(client.getDbUrl()).thenReturn("test");
        when(client.getDbUser()).thenReturn("test");

        when(client.getDbPassword()).thenReturn(null);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.doCreateDataSourceFromClient(client);
        });

        when(client.getDbPassword()).thenReturn("");

        exception = assertThrows(IllegalStateException.class, () -> {
            Object returned = routingDataSource.doCreateDataSourceFromClient(client);
        });


    }


    @Test
    void oCreateDataSourceFromClientEmtpyUrl() {

        Client client = mock(Client.class);

        when(client.getDbUrl()).thenReturn("test");
        when(client.getDbUser()).thenReturn("test");
        when(client.getDbPassword()).thenReturn("test");

        DriverManagerDataSource returned = routingDataSource.doCreateDataSourceFromClient(client);

        assertEquals(client.getDbUrl(), returned.getUrl());
        assertEquals(client.getDbUser(), returned.getUsername());
        assertEquals(client.getDbPassword(), returned.getPassword());

    }


    @Test
    void closeAllConnectionNoCustom() {

        DataSource custom = mock(DataSource.class);
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);
        Set<Map.Entry<DataSourceEnum, DataSource>> set = mock(Set.class);
        Iterator<Map.Entry<DataSourceEnum, DataSource>> iterator = mock(Iterator.class);
        Map.Entry<DataSourceEnum, DataSource> entry = mock(Map.Entry.class);

        doReturn(dataSources).when(routingDataSource).getDataSources();
        when(set.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);

        when(iterator.next()).thenReturn(entry);
        when(entry.getKey()).thenReturn(DataSourceEnum.HSQL);

        when(dataSources.entrySet()).thenReturn(set);

        routingDataSource.closeAllConnection();


        verify(dataSources, times(0)).remove(any());
        verify(routingDataSource, times(0)).closeConnection(any());


    }

    @Test
    void closeAllConnectionCustom() {

        DataSource custom = mock(DataSource.class);
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);
        Set<Map.Entry<DataSourceEnum, DataSource>> set = mock(Set.class);
        Iterator<Map.Entry<DataSourceEnum, DataSource>> iterator = mock(Iterator.class);
        Map.Entry<DataSourceEnum, DataSource> entry = mock(Map.Entry.class);

        doReturn(dataSources).when(routingDataSource).getDataSources();
        doReturn(Boolean.TRUE).when(routingDataSource).closeConnection(any());
        when(set.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);

        when(iterator.next()).thenReturn(entry);
        when(entry.getKey()).thenReturn(DataSourceEnum.CUSTOM);

        when(dataSources.entrySet()).thenReturn(set);


        routingDataSource.closeAllConnection();


        verify(dataSources, times(1)).remove(any());
        verify(routingDataSource, times(1)).closeConnection(any());


    }


    @Test
    void closeConnectionNotNull() {

        DataSource custom = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        try {
            when(custom.getConnection()).thenReturn(connection);
            boolean result = routingDataSource.closeConnection(custom);

            assertEquals(Boolean.TRUE, result);

            verify(connection, times(1)).close();
        } catch (SQLException ex) {

        }


    }

    @Test
    void closeConnectionNull() {

        DataSource custom = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        try {
            when(custom.getConnection()).thenReturn(connection);
            doThrow(SQLException.class).when(connection).close();

            boolean result = routingDataSource.closeConnection(custom);
            assertEquals(Boolean.FALSE, result);

            verify(connection, times(1)).close();
        } catch (SQLException ex) {

        }


    }


    @Test
    void doMigrationError() {

        Client client = mock(Client.class);
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        when(client.getDbUrl()).thenReturn("test");
        when(client.getDbUser()).thenReturn("test");
        when(client.getDbPassword()).thenReturn("test");

        boolean result = routingDataSource.doMigration(client, dataSource);

        assertEquals(Boolean.FALSE, result);
        verify(routingDataSource, times(0)).getDataSources();


    }

    @Test
    void determineTargetDataSource() {
        DataSourceEnum lookupKey = DataSourceEnum.CUSTOM;
        EnumMap<DataSourceEnum, DataSource> dataSources = mock(EnumMap.class);
        DataSource ds = mock(DataSource.class);
        when(dataSources.get(DataSourceEnum.CUSTOM)).thenReturn(ds);
        doReturn(dataSources).when(routingDataSource).getDataSources();
        when(dataSources.containsKey(DataSourceEnum.CUSTOM)).thenReturn(Boolean.TRUE);

        DataSource returned = routingDataSource.determineTargetDataSource();

        assertEquals(returned, ds);
    }


}