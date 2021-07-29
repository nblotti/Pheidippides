package ch.nblotti.pheidippides.kafka.quote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class QuoteDeserializer implements Deserializer<QuoteWrapper> {


    public static final String AFTER = "after";
    public static final String OP = "op";
    public static final String PAYLOAD = "payload";
    public static final String CODE = "code";
    private final ObjectMapper mapper = new ObjectMapper();



    @Override
    public QuoteWrapper deserialize(String s, byte[] in) {

        JsonNode content;
        QuoteWrapper quote = new QuoteWrapper();
        quote.setIn(in);

        try {

            if (in == null)
                quote.setOperation(SQL_OPERATION.EMPTY);
            else {
                content = mapper.readTree(in).get(PAYLOAD);
                SQL_OPERATION operation = SQL_OPERATION.fromString(content.get(OP).asText());

                quote.setOperation(operation);

                if (!operation.equals(SQL_OPERATION.DELETE)) {
                    String code = content.get(AFTER).get(CODE).asText();
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
