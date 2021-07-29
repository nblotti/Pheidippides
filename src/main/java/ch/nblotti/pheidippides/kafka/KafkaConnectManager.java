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
    private final String connectorQuoteUrl;
    private final String connectorUrl;
    private String connectPayload;
    private String quoteTopicFiltred;


    public ResponseEntity<String> initStockConnector(ClientDTO clientDTO) {

        String formatedConnectPayload = buildConnnectorPayload(clientDTO);

        String formatedConnectorQuoteUrl = String.format(connectorQuoteUrl, clientDTO.getUserName());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(formatedConnectPayload, headers);

        try {
            return restTemplate.getForEntity(formatedConnectorQuoteUrl, String.class);

        } catch (HttpStatusCodeException exception) {
            return restTemplate.exchange(connectorUrl, HttpMethod.POST, entity, String.class);
        }

    }

    public boolean deleteStockConnector(ClientDTO clientDTO) {
        String formatedConnectorMonthlyQuoteUrl = String.format(connectorQuoteUrl, clientDTO.getUserName());
        try {
            restTemplate.delete(formatedConnectorMonthlyQuoteUrl);
            return true;

        } catch (HttpStatusCodeException exception) {
            log.error(exception.getMessage());
            return false;
        }

    }


    String buildConnnectorPayload(ClientDTO clientDTO) {
        return String.format(connectPayload, clientDTO.getUserName(), clientDTO.getDbUrl(), clientDTO.getUserName(),clientDTO.getDbUser(), clientDTO.getDbPassword());
    }


}
