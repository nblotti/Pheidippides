package ch.nblotti.pheidippides.kafka.quote;

public interface QuoteFilter {

    public boolean filter(byte[] key,byte[] value);
}
