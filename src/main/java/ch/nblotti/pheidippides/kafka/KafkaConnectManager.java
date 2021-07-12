package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@AllArgsConstructor
@Slf4j
public class KafkaConnectManager {

    RestTemplate restTemplate;
    private final String connectorMonthlyQuoteUrl;
    public String connectorUrl;
    private String weeklyConnectPayload;
    private String monthlyConnectPayload;
    public String monthlyQuoteTopic;


    public ResponseEntity<String> initMonthlyStockConnector(ClientDTO clientDTO) {

        String formatedMonthlyConnectPayload = buildConnnectorPayload(clientDTO);

        String formatedConnectorMonthlyQuoteUrl = String.format(connectorMonthlyQuoteUrl, clientDTO.getUserName());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(formatedMonthlyConnectPayload, headers);

        try {
            return restTemplate.getForEntity(formatedConnectorMonthlyQuoteUrl, String.class);

        } catch (HttpStatusCodeException exception) {
            return restTemplate.exchange(connectorUrl, HttpMethod.POST, entity, String.class);
        }

    }

    public void deleteMonthlyStockConnector(ClientDTO clientDTO) {
        String formatedConnectorMonthlyQuoteUrl = String.format(connectorMonthlyQuoteUrl, clientDTO.getUserName());
        try {
            restTemplate.delete(formatedConnectorMonthlyQuoteUrl);

        } catch (HttpStatusCodeException exception) {
            log.error(exception.getMessage());
        }

    }


    String buildConnnectorPayload(ClientDTO clientDTO) {
        String weeklyFullPayload = String.format(monthlyConnectPayload, clientDTO.getUserName(), clientDTO.getDbUrl(), clientDTO.getDbUser(), clientDTO.getDbPassword());
        return weeklyFullPayload;
    }


}
