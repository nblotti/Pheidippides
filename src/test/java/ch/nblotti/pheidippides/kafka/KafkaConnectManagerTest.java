package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientDTO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class KafkaConnectManagerTest {


    RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    String connectorquoteUrl = "http://kafka1:8083/connectors/";
    String connectorUrl = "kafka1:9092,kafka3:9092";
    String monthlyCc = "{\"name\": \"%s-postgres-stock_monthly_quote-sink\",\"config\": {\"connector.class\": \"io.confluent.connect.jdbc.JdbcSinkConnector\",\"tasks.max\": \"1\",\"connection.url\": \"%s\",\"topics\": \"%s_stock_monthly_quote_filtred\",\"connection.user\": \"%s\",\"connection.password\": \"%s\",\"transforms\": \"unwrap\",\"transforms.unwrap.type\": \"io.debezium.transforms.ExtractNewRecordState\",\"transforms.unwrap.drop.tombstones\":\"false\",\"table.name.format\":\"stock_monthly_quote\",\"insert.mode\": \"upsert\",\"delete.enabled\": \"true\",\"pk.mode\": \"record_key\",\"pk.fields\": \"id\",\"value.converter\":\"org.apache.kafka.connect.json.JsonConverter\",\"value.converter.schemas.enable\": \"true\",\"key.converter\":\"org.apache.kafka.connect.json.JsonConverter\",\"key.converter.schemas.enable\": \"true\"}}";
    String quoteTopic = "stock_monthly_quote";
    KafkaConnectManager kafkaConnectManager = new KafkaConnectManager(restTemplate, connectorquoteUrl, connectorUrl, monthlyCc, quoteTopic);

    @Test
    void buildConnnectorPayload() {

        ClientDTO clientDTO = Mockito.mock(ClientDTO.class);

        when(clientDTO.getUserName()).thenReturn("client1");
        when(clientDTO.getDbUser()).thenReturn("postgres");
        when(clientDTO.getDbPassword()).thenReturn("postgres");
        when(clientDTO.getDbUrl()).thenReturn("jdbc:postgresql://delosdb.coenmrmhbaiw.us-east-2.rds.amazonaws.com:5432/securities");


        String returned = kafkaConnectManager.buildConnnectorPayload(clientDTO);

        Assert.assertEquals(returned, "{\"name\": \"client1-postgres-stock_monthly_quote-sink\",\"config\": {\"connector.class\": \"io.confluent.connect.jdbc.JdbcSinkConnector\",\"tasks.max\": \"1\",\"connection.url\": \"jdbc:postgresql://delosdb.coenmrmhbaiw.us-east-2.rds.amazonaws.com:5432/securities\",\"topics\": \"client1_stock_monthly_quote_filtred\",\"connection.user\": \"postgres\",\"connection.password\": \"postgres\",\"transforms\": \"unwrap\",\"transforms.unwrap.type\": \"io.debezium.transforms.ExtractNewRecordState\",\"transforms.unwrap.drop.tombstones\":\"false\",\"table.name.format\":\"stock_monthly_quote\",\"insert.mode\": \"upsert\",\"delete.enabled\": \"true\",\"pk.mode\": \"record_key\",\"pk.fields\": \"id\",\"value.converter\":\"org.apache.kafka.connect.json.JsonConverter\",\"value.converter.schemas.enable\": \"true\",\"key.converter\":\"org.apache.kafka.connect.json.JsonConverter\",\"key.converter.schemas.enable\": \"true\"}}");

    }


    @Test
    void initMonthlyStockConnector() {
        ClientDTO clientDTO = Mockito.mock(ClientDTO.class);
        ResponseEntity<String> responseEntity = Mockito.mock(ResponseEntity.class);

        when(clientDTO.getUserName()).thenReturn("client1");
        when(clientDTO.getDbUser()).thenReturn("postgres");
        when(clientDTO.getDbPassword()).thenReturn("postgres");
        when(clientDTO.getDbUrl()).thenReturn("jdbc:postgresql://delosdb.coenmrmhbaiw.us-east-2.rds.amazonaws.com:5432/securities");


        when(restTemplate.getForEntity(connectorquoteUrl, String.class)).thenReturn(responseEntity);


        ResponseEntity<String> returned = kafkaConnectManager.initStockConnector(clientDTO);

        assertEquals(responseEntity, returned);

    }

    @Test
    void initMonthlyExistingStockConnector() {
        ClientDTO clientDTO = Mockito.mock(ClientDTO.class);
        ResponseEntity<String> responseEntity = Mockito.mock(ResponseEntity.class);

        when(clientDTO.getUserName()).thenReturn("client1");
        when(clientDTO.getDbUser()).thenReturn("postgres");
        when(clientDTO.getDbPassword()).thenReturn("postgres");
        when(clientDTO.getDbUrl()).thenReturn("jdbc:postgresql://delosdb.coenmrmhbaiw.us-east-2.rds.amazonaws.com:5432/securities");


        when(restTemplate.getForEntity(connectorquoteUrl, String.class)).thenThrow(HttpServerErrorException.class);
        ResponseEntity<String> ok = ResponseEntity.of(Optional.of("ok"));

        when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(ok);


        ResponseEntity<String> returned = kafkaConnectManager.initStockConnector(clientDTO);


        assertEquals(returned, ok);

    }


}
