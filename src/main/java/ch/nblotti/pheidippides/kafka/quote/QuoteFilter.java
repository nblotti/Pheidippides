package ch.nblotti.pheidippides.kafka.quote;

public interface QuoteFilter<S, T> {

    public boolean filter(S key, T value);
}
