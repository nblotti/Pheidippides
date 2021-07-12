package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.MonthlyQuoteFilter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;


class PheidippidesMonthlyTopologyTest {


    public String monthlyQuoteTopic = "monthlyQuoteTopic";
    public String monthlyQuoteTopicFiltred = "monthlyQuoteTopicFiltred";

    TopologyTestDriver testDriver;

    @Mock
    MonthlyQuoteFilter quoteFilter;


    @BeforeEach
    void beforeEach() {

        MockitoAnnotations.openMocks(this);

        PheidippidesMonthlyTopology pheidippidesMonthlyTopology = new PheidippidesMonthlyTopology(quoteFilter, monthlyQuoteTopic, monthlyQuoteTopicFiltred);

        Topology topology = pheidippidesMonthlyTopology.getTopology();
        testDriver = new TopologyTestDriver(topology);

    }


    //https://kafka.apache.org/documentation/streams/developer-guide/testing.html
    @Test
    public void kafkaStreamRead() {


        TestInputTopic<byte[], byte[]> inputTopic = testDriver.createInputTopic(monthlyQuoteTopic, Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer());
        TestOutputTopic<byte[], byte[]> outputTopic = testDriver.createOutputTopic(monthlyQuoteTopicFiltred, Serdes.ByteArray().deserializer(), Serdes.ByteArray().deserializer());

        doReturn(Boolean.TRUE).when(quoteFilter).filter(any(), any());

        byte[] key = "key".getBytes(StandardCharsets.UTF_8);
        byte[] value = "Value".getBytes(StandardCharsets.UTF_8);

        inputTopic.pipeInput(key, value);

        KeyValue<byte[], byte[]> returned = outputTopic.readKeyValue();
        Assert.assertEquals(returned.key, key);
        Assert.assertEquals(returned.value, value);

    }

    //https://kafka.apache.org/documentation/streams/developer-guide/testing.html
    @Test
    public void kafkaStreamReadNoFilter() {


        TestInputTopic<byte[], byte[]> inputTopic = testDriver.createInputTopic(monthlyQuoteTopic, Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer());
        TestOutputTopic<byte[], byte[]> outputTopic = testDriver.createOutputTopic(monthlyQuoteTopicFiltred, Serdes.ByteArray().deserializer(), Serdes.ByteArray().deserializer());

        doReturn(Boolean.FALSE).when(quoteFilter).filter(any(), any());

        byte[] key = "key".getBytes(StandardCharsets.UTF_8);
        byte[] value = "Value".getBytes(StandardCharsets.UTF_8);

        try{
            inputTopic.pipeInput(key, value);
            KeyValue<byte[], byte[]> returned = outputTopic.readKeyValue();
            fail();
        }
        catch (NoSuchElementException ex){
            return;
        }

        fail();


    }


}