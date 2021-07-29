package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.QuoteDeserializer;
import ch.nblotti.pheidippides.kafka.quote.QuoteWrapper;
import ch.nblotti.pheidippides.kafka.quote.SQL_OPERATION;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class QuoteDeserializerTest {


    QuoteDeserializer quoteDeserializer;

    @BeforeEach
    void beforeEach() {

        quoteDeserializer = new QuoteDeserializer();
    }

    @Test
    void deserializeEmpty() {

        QuoteWrapper quote = quoteDeserializer.deserialize(null, null);

        assertEquals(SQL_OPERATION.EMPTY, quote.getOperation());

    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"payload\":{}}", "{\"payload\":{\"op\":\"r\"}}", "{\"payload\":{\"op\":\"r\",\"after\":{\"exchange\":\"US\"}}}"})
    void deserialize(String in) {

        byte[] value = in.getBytes(StandardCharsets.UTF_8);
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

    @Test
    void deserializeDeleteOperation() {


        byte[] value = ("{\"payload\":{\"op\":\"d\"}}").getBytes(StandardCharsets.UTF_8);
        QuoteWrapper quote = quoteDeserializer.deserialize(null, value);

        assertEquals(SQL_OPERATION.DELETE, quote.getOperation());
        assertNull( quote.getCode());

    }


}