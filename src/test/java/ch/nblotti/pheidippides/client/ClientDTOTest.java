package ch.nblotti.pheidippides.client;

import lombok.Getter;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.StateMachine;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientDTOTest {


    public static final String USER = "User";
    public static final String URL = "URL";
    public static final String PASSWORD = "Password";

    @Test
    void subscribe() {

         String userName = "nblotti";
         ClientDBInfo clientDBInfo = mock(ClientDBInfo.class);

         List<StrategiesDTO> strategies = Collections.emptyList();

        when(clientDBInfo.getDbUser()).thenReturn(USER);
        when(clientDBInfo.getDbUrl()).thenReturn(URL);
        when(clientDBInfo.getDbPassword()).thenReturn(PASSWORD);

        ClientDTO clientDTO = new ClientDTO(userName,clientDBInfo,strategies);

        Assert.assertEquals(USER,clientDTO.getDbUser());
        Assert.assertEquals(URL,clientDTO.getDbUrl());
        Assert.assertEquals(PASSWORD,clientDTO.getDbPassword());
    }
}