package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientTO;
import ch.nblotti.pheidippides.kafka.quote.*;
import ch.nblotti.pheidippides.kafka.user.UserSubscription;
import ch.nblotti.pheidippides.kafka.user.UserSubscriptionSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class PheidippidesTopologyTest {


    public String quoteTopic = "quoteTopic";
    public String monthlyQuoteTopicFiltred = "%s_quoteTopicFiltred";
    public String userSubscriptionTopic = "userSubscriptionTopic";
    public String userSubscriptionTopicFiltred = "userSubscriptionTopicFiltred";


    TopologyTestDriver testDriver;

    QuoteKeySerdes quoteKeySerdes = new QuoteKeySerdes();
    QuoteSerdes quoteSerdes = new QuoteSerdes();


    @Mock
    QuoteFilterImpl quoteFilter;

    @Mock
    ClientTO clientTO;


    @BeforeEach
    void beforeEach() {

        MockitoAnnotations.openMocks(this);


    }


    //https://kafka.apache.org/documentation/streams/developer-guide/testing.html

    @Test
    void kafkaStreamRead() {

        Properties streamConfiguration = new Properties();

        streamConfiguration.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamConfiguration.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        PheidippidesTopology pheidippidesTopology = new PheidippidesTopology(quoteTopic, monthlyQuoteTopicFiltred, userSubscriptionTopic, userSubscriptionTopicFiltred);
        UserSubscriptionSerdes userSubscriptionSerdes = new UserSubscriptionSerdes();

        doReturn(Boolean.TRUE).when(quoteFilter).filter(any(), any());
        when(clientTO.getUserName()).thenReturn("client1");

        Topology topology = pheidippidesTopology.getTopology(clientTO, "internalMapTopicName", "internalTransfodTopicName");
        testDriver = new TopologyTestDriver(topology, streamConfiguration);


        TestInputTopic<String, UserSubscription> userSubscriptions = testDriver.createInputTopic(userSubscriptionTopic, Serdes.String().serializer(), userSubscriptionSerdes.serializer());

        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setStocks(Arrays.asList("AAPL", "GOOGL", "YAHOO"));
        userSubscription.setEtfs(null);
        userSubscriptions.pipeInput("client1", userSubscription);


        String firstQuoteValue = "{\"payload\":{\"after\":{\"id\":\"1\",\"exchange\":\"US\",\"code\":\"YAHOO\",\"gic_sector\":\"GIC\",\"month_number\":\"1\",\"year\":\"2021\",\"type\":\"MONTH\",\"median_adjusted_close\":\"1.0\",\"median_market_cap\":\"1.0\",\"median_volume\":\"1.0\",\"avg_adjusted_close\":\"1.0\",\"avg_market_cap\":\"1.0\",\"avg_volume\":\"1.0\",\"updated_date\":\"18628\"},\"op\":\"c\",\"ts_ms\":\"1627123020382\",\"transaction\":\"null\"}}";

        String secondQuoteValue = "{\"payload\":{\"after\":{\"id\":\"1\",\"exchange\":\"US\",\"code\":\"FB\",\"gic_sector\":\"GIC\",\"month_number\":\"1\",\"year\":\"2021\",\"type\":\"MONTH\",\"median_adjusted_close\":\"1.0\",\"median_market_cap\":\"1.0\",\"median_volume\":\"1.0\",\"avg_adjusted_close\":\"1.0\",\"avg_market_cap\":\"1.0\",\"avg_volume\":\"1.0\",\"updated_date\":\"18628\"},\"op\":\"c\",\"ts_ms\":\"1627123020382\",\"transaction\":\"null\"}}";

        String thirdQuoteValue = "{\"payload\":{\"after\":{\"id\":\"1\",\"exchange\":\"US\",\"code\":\"GOOGL\",\"gic_sector\":\"GIC\",\"month_number\":\"1\",\"year\":\"2021\",\"type\":\"MONTH\",\"median_adjusted_close\":\"1.0\",\"median_market_cap\":\"1.0\",\"median_volume\":\"1.0\",\"avg_adjusted_close\":\"1.0\",\"avg_market_cap\":\"1.0\",\"avg_volume\":\"1.0\",\"updated_date\":\"18628\"},\"op\":\"c\",\"ts_ms\":\"1627123020382\",\"transaction\":\"null\"}}";


        TestInputTopic<byte[], byte[]> quoteTopic = testDriver.createInputTopic(this.quoteTopic, Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer());


        quoteTopic.pipeInput("keyKey1".getBytes(StandardCharsets.UTF_8), firstQuoteValue.getBytes(StandardCharsets.UTF_8));

        quoteTopic.pipeInput("keyKey2".getBytes(StandardCharsets.UTF_8), secondQuoteValue.getBytes(StandardCharsets.UTF_8));

        quoteTopic.pipeInput("keyKey3".getBytes(StandardCharsets.UTF_8), thirdQuoteValue.getBytes(StandardCharsets.UTF_8));

        String quoteTopicFiltredStr = String.format(monthlyQuoteTopicFiltred, clientTO.getUserName());

        TestOutputTopic<QuoteKeyWrapper, QuoteWrapper> outputTopic = testDriver.createOutputTopic(quoteTopicFiltredStr, quoteKeySerdes.deserializer(), quoteSerdes.deserializer());

        KeyValue<QuoteKeyWrapper, QuoteWrapper> firstResult = outputTopic.readKeyValue();
        Assert.assertEquals("keyKey1", new String(firstResult.key.getIn()));
        Assert.assertEquals("YAHOO", firstResult.value.getCode());
        Assert.assertEquals(SQL_OPERATION.CREATE, firstResult.value.getOperation());

        KeyValue<QuoteKeyWrapper, QuoteWrapper> secondResult = outputTopic.readKeyValue();
        Assert.assertEquals("keyKey3", new String(secondResult.key.getIn()));
        Assert.assertEquals("GOOGL", secondResult.value.getCode());
        Assert.assertEquals(SQL_OPERATION.CREATE, secondResult.value.getOperation());


        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            outputTopic.readKeyValue();
        });


    }


}