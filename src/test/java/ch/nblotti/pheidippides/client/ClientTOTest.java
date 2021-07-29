package ch.nblotti.pheidippides.client;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientTOTest {


    public static final String USER = "User";
    public static final String URL = "URL";
    public static final String PASSWORD = "Password";

    @Test
    void getDbUser() {

        String userName = "nblotti";
        ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);

        List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbUser()).thenReturn(USER);

        ClientTO clientTO = new ClientTO(userName, clientDBInfo, strategies);

        Assert.assertEquals(USER, clientTO.getDbUser());
    }
    @Test
    void getDbUrl() {

        String userName = "nblotti";
        ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);
        List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbUrl()).thenReturn(URL);

        ClientTO clientTO = new ClientTO(userName, clientDBInfo, strategies);

        Assert.assertEquals(URL, clientTO.getDbUrl());
    }
    @Test
    void getDbPassword() {

        String userName = "nblotti";
        ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);

        List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbPassword()).thenReturn(PASSWORD);

        ClientTO clientTO = new ClientTO(userName, clientDBInfo, strategies);

        Assert.assertEquals(PASSWORD, clientTO.getDbPassword());
    }
}