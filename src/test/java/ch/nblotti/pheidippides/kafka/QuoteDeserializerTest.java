package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.QuoteDeserializer;
import ch.nblotti.pheidippides.kafka.quote.QuoteWrapper;
import ch.nblotti.pheidippides.kafka.quote.SQL_OPERATION;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuoteDeserializerTest {


    QuoteDeserializer quoteDeserializer;

    @BeforeEach
    void beforeEach() {

        quoteDeserializer = new QuoteDeserializer();
        //   Mockito.lenient().when(settingRepository.getUserMinAge()).thenReturn(10);
    }

    @Test
    void deserializeEmpty() {

        QuoteWrapper quote = quoteDeserializer.deserialize(null, null);

        assertEquals(SQL_OPERATION.EMPTY, quote.getOperation());

    }

    @Test
    void deserializeNoPayload() {

        byte[] value = "{}".getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.ERROR, quote.getOperation());

    }

    @Test
    void deserializeNoOperation() {

        byte[] value = "{\"payload\":{}}".getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.ERROR, quote.getOperation());

    }

    @Test
    void deserializeOperationNoAfterExchange() {

        byte[] value = "{\"payload\":{\"op\":\"r\"}}".getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.ERROR, quote.getOperation());

    }


    @Test
    void deserializeOperationNoExchange() {

        byte[] value = "{\"payload\":{\"op\":\"r\",\"after\":{\"code\":\"AAPL\"}}}".getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.ERROR, quote.getOperation());

    }

    @Test
    void deserializeOperationNoCode() {

        byte[] value = "{\"payload\":{\"op\":\"r\",\"after\":{\"exchange\":\"US\"}}}".getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.ERROR, quote.getOperation());

    }

    @Test
    void deserializeOperation() {

        String aapl = "AAPL";
        String us = "US";

        byte[] value = ("{\"payload\":{\"op\":\"r\",\"after\":{\"exchange\":\"" + us + "\",\"code\":\"" + aapl + "\"}}}").getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.READ, quote.getOperation());
        assertEquals(aapl, quote.getCode());

    }


}