package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.QuoteFilter;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

@AllArgsConstructor
@Setter
public class PheidippidesMonthlyTopology {

    public QuoteFilter quoteFilter;
    public String quoteTopic;
    public String quoteTopicFiltred;


    public Topology getTopology() {

        final StreamsBuilder builder = new StreamsBuilder();


        KStream<byte[], byte[]> stream = builder.stream(quoteTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

        builder.stream(quoteTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()))
                .filter((key, value) -> {

                    return this.quoteFilter.filter(key, value);
                })
                .to(quoteTopicFiltred);


        return builder.build();
    }


}
