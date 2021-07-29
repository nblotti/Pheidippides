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
class ClientDTOTest {


    public static final String USER = "User";
    public static final String URL = "URL";
    public static final String PASSWORD = "Password";

    @Test
    void getDbUser() {

        String userName = "nblotti";
        ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);

        List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbUser()).thenReturn(USER);

        ClientDTO clientDTO = new ClientDTO(userName, clientDBInfo, strategies);

        Assert.assertEquals(USER, clientDTO.getDbUser());
    }
    @Test
    void getDbUrl() {

        String userName = "nblotti";
        ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);
        List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbUrl()).thenReturn(URL);

        ClientDTO clientDTO = new ClientDTO(userName, clientDBInfo, strategies);

        Assert.assertEquals(URL, clientDTO.getDbUrl());
    }
    @Test
    void getDbPassword() {

        String userName = "nblotti";
        ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);

        List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbPassword()).thenReturn(PASSWORD);

        ClientDTO clientDTO = new ClientDTO(userName, clientDBInfo, strategies);

        Assert.assertEquals(PASSWORD, clientDTO.getDbPassword());
    }
}