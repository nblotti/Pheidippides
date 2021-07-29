package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


public class MonthlyQuoteFilterImplTest {


    @Mock
    QuoteDeserializer quoteDeserializer;

    private QuoteFilterImpl quoteFilterImpl;


    @BeforeEach
    void beforeEach() {

        MockitoAnnotations.openMocks(this);
        quoteFilterImpl = new QuoteFilterImpl();

    }

    @Test
    void filterEmptyEntry() {

        QuoteKeyWrapper quoteKey = Mockito.mock(QuoteKeyWrapper.class);
        QuoteWrapper quote = Mockito.mock(QuoteWrapper.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.EMPTY);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = quoteFilterImpl.filter(quoteKey, quote);

        Assert.assertTrue(returned);
    }

    @Test
    void filterErrorEntry() {

        QuoteKeyWrapper quoteKey = Mockito.mock(QuoteKeyWrapper.class);
        QuoteWrapper quote = Mockito.mock(QuoteWrapper.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.ERROR);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = quoteFilterImpl.filter(quoteKey, quote);

        Assert.assertFalse(returned);
    }

    @Test
    void filterDeleteEntry() {

        QuoteKeyWrapper quoteKey = Mockito.mock(QuoteKeyWrapper.class);
        QuoteWrapper quote = Mockito.mock(QuoteWrapper.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.DELETE);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = quoteFilterImpl.filter(quoteKey, quote);

        Assert.assertTrue(returned);
    }

    void filterReadEntry() {

        QuoteWrapper quote = Mockito.mock(QuoteWrapper.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.READ);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = quoteFilterImpl.filter(null, null);

        Assert.assertFalse(returned);
    }


}