package ch.nblotti.pheidippides.kafka.quote;

import static ch.nblotti.pheidippides.kafka.quote.SQL_OPERATION.EMPTY;

public class QuoteFilterImpl implements QuoteFilter<QuoteKeyWrapper, QuoteWrapper> {


    @Override
    public boolean filter(QuoteKeyWrapper key, QuoteWrapper value) {


        //Thombstone delete event
        if (key != null && value == null)
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
