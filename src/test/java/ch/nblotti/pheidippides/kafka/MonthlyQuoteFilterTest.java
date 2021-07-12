package ch.nblotti.pheidippides.kafka;

import ch.nblotti.pheidippides.kafka.quote.MonthlyQuoteFilter;
import ch.nblotti.pheidippides.kafka.quote.Quote;
import ch.nblotti.pheidippides.kafka.quote.QuoteDeserializer;
import ch.nblotti.pheidippides.kafka.quote.SQL_OPERATION;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


public class MonthlyQuoteFilterTest {


    @Mock
    QuoteDeserializer quoteDeserializer;

    private MonthlyQuoteFilter monthlyQuoteFilter;


    @BeforeEach
    void beforeEach() {

        MockitoAnnotations.openMocks(this);
        monthlyQuoteFilter = new MonthlyQuoteFilter(quoteDeserializer);

    }

    @Test
    public void filterEmptyEntry() {

        Quote quote = Mockito.mock(Quote.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.EMPTY);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = monthlyQuoteFilter.filter(null, null);

        Assert.assertTrue(returned);
    }

    @Test
    public void filterErrorEntry() {

        Quote quote = Mockito.mock(Quote.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.ERROR);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = monthlyQuoteFilter.filter(null, null);

        Assert.assertFalse(returned);
    }

    @Test
    public void filterDeleteEntry() {

        Quote quote = Mockito.mock(Quote.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.DELETE);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = monthlyQuoteFilter.filter(null, null);

        Assert.assertTrue(returned);
    }

    public void filterReadEntry() {

        Quote quote = Mockito.mock(Quote.class);
        when(quote.getOperation()).thenReturn(SQL_OPERATION.READ);

        doReturn(quote).when(quoteDeserializer).deserialize(any(), any());

        boolean returned = monthlyQuoteFilter.filter(null, null);

        Assert.assertFalse(returned);
    }


}