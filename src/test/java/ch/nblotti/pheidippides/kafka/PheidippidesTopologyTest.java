package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.kafka.quote.QuoteFilterImpl;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


class PheidippidesTopologyTest {


    public String monthlyQuoteTopic = "monthlyQuoteTopic";
    public String monthlyQuoteTopicFiltred = "monthlyQuoteTopicFiltred";
    public String userSubscriptionTopic = "userSubscriptionTopic";

    TopologyTestDriver testDriver;

    @Mock
    QuoteFilterImpl quoteFilter;

    @Mock
    ClientDTO clientDTO;

    @Mock
    Properties streamConfiguration;


    @BeforeEach
    void beforeEach() {

        MockitoAnnotations.openMocks(this);

        PheidippidesTopology pheidippidesTopology = new PheidippidesTopology( monthlyQuoteTopic, monthlyQuoteTopicFiltred,userSubscriptionTopic);

        Topology topology = pheidippidesTopology.getTopology(clientDTO,streamConfiguration,"%s","%s","%s");
        testDriver = new TopologyTestDriver(topology);

    }


    //https://kafka.apache.org/documentation/streams/developer-guide/testing.html

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