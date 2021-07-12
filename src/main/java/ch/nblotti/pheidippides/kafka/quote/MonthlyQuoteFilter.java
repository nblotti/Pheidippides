package ch.nblotti.pheidippides.kafka.quote;

import org.springframework.beans.factory.annotation.Autowired;

public class MonthlyQuoteFilter implements QuoteFilter {


    private final QuoteDeserializer quoteDeserializer;


    @Autowired
    public MonthlyQuoteFilter(QuoteDeserializer quoteDeserializer) {
        this.quoteDeserializer = quoteDeserializer;
    }

    @Override
    public boolean filter(byte[] key, byte[] value) {
        Quote quote = quoteDeserializer.deserialize(key, value);

        if (quote.getOperation().equals(SQL_OPERATION.EMPTY))
            return true;
        if (quote.getOperation().equals(SQL_OPERATION.ERROR))
            return false;
        if (quote.getOperation().equals(SQL_OPERATION.DELETE))
            return true;
        if (quote.getOperation().equals(SQL_OPERATION.CREATE))
            return true;


        return false;
    }


}
