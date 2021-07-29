package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.Client;
import ch.nblotti.pheidippides.kafka.quote.*;
import ch.nblotti.pheidippides.kafka.user.UserSubscription;
import ch.nblotti.pheidippides.kafka.user.UserSubscriptionSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Predicate;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
    Client client;


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
        when(client.getUserName()).thenReturn("client1");

        Topology topology = pheidippidesTopology.getTopology(client, "internalMapTopicName", "internalTransfodTopicName");
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

        String quoteTopicFiltredStr = String.format(monthlyQuoteTopicFiltred, client.getUserName());

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



    @Test
    void thombstoneOrDeleteKeyNull() {


        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.thombstoneOrDeleteOperationPredicate();

        QuoteKeyWrapper quoteKeyWrapper = null;
        QuoteWrapper quoteWrapper  = null;

        Assert.assertFalse(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }

    @Test
    void thombstoneOrDeleteKeyNotNullOperationNull() {


        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.thombstoneOrDeleteOperationPredicate();

        QuoteKeyWrapper quoteKeyWrapper = mock(QuoteKeyWrapper.class);
        QuoteWrapper quoteWrapper = null;

        Assert.assertTrue(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }


    @ParameterizedTest
    @EnumSource(value = SQL_OPERATION.class, names = {"DELETE", "EMPTY"})
    void thombstoneOrDeleteOperationPredicateFalse(SQL_OPERATION op) {


        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.thombstoneOrDeleteOperationPredicate();

        QuoteKeyWrapper quoteKeyWrapper = mock(QuoteKeyWrapper.class);
        QuoteWrapper quoteWrapper  = mock(QuoteWrapper.class);

        when(quoteWrapper.getOperation()).thenReturn(op);

        Assert.assertTrue(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }

    @ParameterizedTest
    @EnumSource(value = SQL_OPERATION.class, names = {"CREATE", "ERROR","READ", "UPDATE"})
    void thombstoneOrDeleteOperationPredicateEnumTrue(SQL_OPERATION op) {


        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.thombstoneOrDeleteOperationPredicate();

        QuoteKeyWrapper quoteKeyWrapper = mock(QuoteKeyWrapper.class);
        QuoteWrapper quoteWrapper  = mock(QuoteWrapper.class);

        when(quoteWrapper.getOperation()).thenReturn(op);

        Assert.assertFalse(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }



    @Test
    void operationToFilterPredicateKeyNull() {

        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.operationToFilterPredicate();

        QuoteKeyWrapper quoteKeyWrapper = null;
        QuoteWrapper quoteWrapper  = mock(QuoteWrapper.class);

        Assert.assertFalse(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }

    @Test
    void operationToFilterPredicateKeyNotNullValueNull() {

        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.operationToFilterPredicate();

        QuoteKeyWrapper quoteKeyWrapper = mock(QuoteKeyWrapper.class);
        QuoteWrapper quoteWrapper  = null;

        Assert.assertFalse(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }


    @Test
    void operationToFilterPredicateKeyNotNullValueNotNullOperationDelete() {

        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.operationToFilterPredicate();

        QuoteKeyWrapper quoteKeyWrapper = mock(QuoteKeyWrapper.class);
        QuoteWrapper quoteWrapper  =  mock(QuoteWrapper.class);

        when(quoteWrapper.getOperation()).thenReturn(SQL_OPERATION.DELETE);

        Assert.assertFalse(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }

    @ParameterizedTest
    @EnumSource(value = SQL_OPERATION.class, names = {"CREATE", "ERROR","READ", "UPDATE","EMPTY"})
    void operationToFilterPredicateKeyNotNullValueNotNullOtherOperation(SQL_OPERATION op) {

        Predicate<QuoteKeyWrapper, QuoteWrapper> operationPredicate = PheidippidesTopology.operationToFilterPredicate();

        QuoteKeyWrapper quoteKeyWrapper = mock(QuoteKeyWrapper.class);
        QuoteWrapper quoteWrapper  =  mock(QuoteWrapper.class);

        when(quoteWrapper.getOperation()).thenReturn(op);

        Assert.assertTrue(operationPredicate.test(quoteKeyWrapper, quoteWrapper));

    }


}