package ch.nblotti.pheidippides.kafka.quote;

public class QuoteFilterImpl implements QuoteFilter<QuoteKeyWrapper, QuoteWrapper> {


    @Override
    public boolean filter(QuoteKeyWrapper key, QuoteWrapper value) {


        //Thombstone delete event
        if (key != null && value == null || value.getOperation().equals(SQL_OPERATION.EMPTY))
            return true;
        if (value.getOperation().equals(SQL_OPERATION.ERROR))
            return false;
        if (value.getOperation().equals(SQL_OPERATION.DELETE))
            return true;
        if (value.getOperation().equals(SQL_OPERATION.CREATE))
            return true;


        return false;
    }


}
