package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.client.ClientDTO;
import ch.nblotti.pheidippides.kafka.container.Container;
import ch.nblotti.pheidippides.kafka.container.ContainerSerdes;
import ch.nblotti.pheidippides.kafka.quote.*;
import ch.nblotti.pheidippides.kafka.user.UserSubscription;
import ch.nblotti.pheidippides.kafka.user.UserSubscriptionSerdes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.internals.Topic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Setter
@Slf4j
public class PheidippidesTopology {


    private String quoteTopic;
    private String quoteTopicFiltred;
    private String userSubscriptionTopic;
    private String userSubscriptionTopicFiltred;


    private ValueJoiner<Container, String, ContainerWithQuote> containerWithQuoteJoiner() {
        return (container, quote) -> {
            return new ContainerWithQuote(container, quote);
        };
    }

    ;

    private Predicate<QuoteKeyWrapper, QuoteWrapper> thombstoneOrDeleteOperationPredicate() {
        return (key, value) -> {
            return (key != null && value == null) || value.getOperation().equals(SQL_OPERATION.DELETE) || value.getOperation().equals(SQL_OPERATION.EMPTY);
        };
    }

    ;

    private Predicate<QuoteKeyWrapper, QuoteWrapper> operationToFilterPredicate() {
        return (key, value) -> {
            return key != null && value != null && !value.getOperation().equals(SQL_OPERATION.DELETE);
        };
    }


    public Topology getTopology(ClientDTO clientDTO, String internalMapTopicName, String internalTransformedTopicName, String userSubscriptionTopicName) {

        final StreamsBuilder builder = new StreamsBuilder();

        QuoteKeySerdes quoteKeySerdes = new QuoteKeySerdes();
        QuoteSerdes quoteSerdes = new QuoteSerdes();


        String quoteTopicFiltredStr = String.format(quoteTopicFiltred, clientDTO.getUserName());


        // get subscribed quotes
        GlobalKTable<String, String> userSubscriptions = getValueSubsribedStream(clientDTO, builder);


        KStream<QuoteKeyWrapper, QuoteWrapper> quoteTopicFiltredTopic = builder.stream(quoteTopic, Consumed.with(quoteKeySerdes, quoteSerdes));

        // split the stream, one branch with delete and thombstone event,  not filtred and one branch with event to filter (otherOperation)
        Map<String, KStream<QuoteKeyWrapper, QuoteWrapper>> branches = quoteTopicFiltredTopic.split(Named.as("split-"))
                .branch(thombstoneOrDeleteOperationPredicate()) // split-1
                .branch(operationToFilterPredicate())// split-2
                .defaultBranch();

        // Regroup key & value of the events in the branch conaining all elements to transform

        KStream<QuoteKeyWrapper, QuoteWrapper> notTransformed = branches.get("split-1");
        branches.get("split-2").map((key, value) -> new KeyValue<String, Container>(value.getCode(), new Container(key, value))).to(internalMapTopicName, Produced.with(Serdes.String(), new ContainerSerdes()));

        KStream<String, Container> toFilter = builder.stream(internalMapTopicName, Consumed.with(Serdes.String(), new ContainerSerdes()));


        KStream<String, ContainerWithQuote> filtred = toFilter.join(userSubscriptions, (String key, Container value) -> {
            return value.getQuoteWrapper().getCode();
        }, containerWithQuoteJoiner());

        //transform to wrapper

        filtred.map((key, value) -> {
            return new KeyValue<>(value.getContainer().getQuoteKeyWrapper(), value.getContainer().getQuoteWrapper());
        }).to(internalTransformedTopicName, Produced.with(quoteKeySerdes, quoteSerdes));

        KStream<QuoteKeyWrapper, QuoteWrapper> transformedAndMerged = builder.stream(internalTransformedTopicName, Consumed.with(quoteKeySerdes, quoteSerdes));

        //merge with delete & thombstone
        KStream<QuoteKeyWrapper, QuoteWrapper> merged = notTransformed.merge(transformedAndMerged);

        //send remerged quotes to the filtred topic
        merged.to(quoteTopicFiltredStr, Produced.with(quoteKeySerdes, quoteSerdes));


        return builder.build();
    }

    @NotNull
    private GlobalKTable<String, String> getValueSubsribedStream(ClientDTO clientDTO, StreamsBuilder builder) {

        UserSubscriptionSerdes userSubscriptionSerdes = new UserSubscriptionSerdes();

        String userSubscriptionTopicFiltredStr = String.format(userSubscriptionTopicFiltred, clientDTO.getUserName());

        Predicate<String, UserSubscription> iscurrentUser = (key, value) -> {
            return key.equals(clientDTO.getUserName());
        };


        KStream<String, UserSubscription> userSubscriptions = builder.stream(userSubscriptionTopic, Consumed.with(Serdes.String(), userSubscriptionSerdes));

        userSubscriptions.print(Printed.toSysOut());

        KStream<String, UserSubscription> users = userSubscriptions.filter(iscurrentUser);

        users.flatMap((key, value) -> {
                    List<String> symbols = Stream.of(value.getStocks(), value.getEtfs())
                            .flatMap(Collection::stream).collect(Collectors.toList());

                    List<KeyValue<String, String>> result = new ArrayList<>(symbols.size());
                    for (String token : symbols) {
                        result.add(new KeyValue<>(token, token));
                    }
                    return result;
                }
        ).to(userSubscriptionTopicFiltredStr, Produced.with(Serdes.String(), Serdes.String()));

        return builder.globalTable(userSubscriptionTopicFiltredStr, Consumed.with(Serdes.String(), Serdes.String()));

    }


    @AllArgsConstructor
    @Getter
    private class ContainerWithQuote {

        Container container;
        String quote;

    }

}
