package ch.nblotti.pheidippides.kafka.quote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class QuoteDeserializer {

    public static final String AFTER = "after";
    public static final String EXCHANGE = "exchange";
    public static final String OP = "op";
    public static final String PAYLOAD = "payload";
    public static final String CODE = "code";
    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode payload;


    public Quote deserialize(byte[] key, byte[] value) {

        Quote quote = new Quote();

        try {

            if (value == null)
                quote.setOperation(SQL_OPERATION.EMPTY);
            else {
                payload = mapper.readTree(value).get(PAYLOAD);
                SQL_OPERATION operation = SQL_OPERATION.fromString(payload.get(OP).asText());

                quote.setOperation(operation);

                if (!operation.equals(SQL_OPERATION.DELETE)) {
                    String exchange = payload.get(AFTER).get(EXCHANGE).asText();
                    String code = payload.get(AFTER).get(CODE).asText();
                    quote.setExchange(exchange);
                    quote.setCode(code);
                }
            }
        } catch (IOException | NullPointerException |
                IllegalStateException e) {
            log.error(e.getMessage());
            quote.setOperation(SQL_OPERATION.ERROR);
        }

        return quote;
    }
}
