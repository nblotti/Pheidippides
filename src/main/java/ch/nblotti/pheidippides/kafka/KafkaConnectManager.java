package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientTO;
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


    public ResponseEntity<String> initStockConnector(ClientTO clientTO) {

        String formatedConnectPayload = buildConnnectorPayload(clientTO);

        String formatedConnectorQuoteUrl = String.format(connectorQuoteUrl, clientTO.getUserName());

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

    public boolean deleteStockConnector(ClientTO clientTO) {
        String formatedConnectorMonthlyQuoteUrl = String.format(connectorQuoteUrl, clientTO.getUserName());
        try {
            restTemplate.delete(formatedConnectorMonthlyQuoteUrl);
            return true;

        } catch (HttpStatusCodeException exception) {
            log.error(exception.getMessage());
            return false;
        }

    }


    String buildConnnectorPayload(ClientTO clientTO) {
        return String.format(connectPayload, clientTO.getUserName(), clientTO.getDbUrl(), clientTO.getUserName(), clientTO.getDbUser(), clientTO.getDbPassword());
    }


}
