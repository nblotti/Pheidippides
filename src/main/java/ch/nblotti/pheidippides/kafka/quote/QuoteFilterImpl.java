package ch.nblotti.pheidippides.kafka.quote;

public class QuoteFilterImpl implements QuoteFilter<QuoteKeyWrapper, QuoteWrapper> {


    @Override
    public boolean filter(QuoteKeyWrapper key, QuoteWrapper value) {


        if (key == null)
            throw new IllegalStateException("key can't be null");

        //Thombstone delete event
        if (value == null)
            return true;

        switch (value.getOperation()) {
            case EMPTY:
            case DELETE:
            case CREATE:
                return true;
            default:
                return false;
        }
    }


}
