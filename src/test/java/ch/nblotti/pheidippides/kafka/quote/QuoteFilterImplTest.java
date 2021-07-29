package ch.nblotti.pheidippides.kafka.quote;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteFilterImplTest {

    QuoteFilter<QuoteKeyWrapper, QuoteWrapper> quoteFilter = new QuoteFilterImpl();


    @Test
    void filterNullValue() {

        QuoteKeyWrapper key = mock(QuoteKeyWrapper.class);
        QuoteWrapper value = null;
        boolean result = quoteFilter.filter(key, value);

        Assert.assertTrue(result);
        //verify(value, times(0)).getOperation();

    }

    @ParameterizedTest
    @EnumSource(value = SQL_OPERATION.class, names = {"EMPTY", "DELETE", "CREATE"})
    void filterTrueValue(SQL_OPERATION operation) {

        QuoteKeyWrapper key = mock(QuoteKeyWrapper.class);
        QuoteWrapper value = mock(QuoteWrapper.class);

        when(value.getOperation()).thenReturn(operation);
        boolean result = quoteFilter.filter(key, value);

        Assert.assertTrue(result);
        verify(value, times(1)).getOperation();

    }

    @ParameterizedTest
    @EnumSource(value = SQL_OPERATION.class, names = {"ERROR", "READ", "UPDATE"})
    void filterFalseValue(SQL_OPERATION operation) {

        QuoteKeyWrapper key = mock(QuoteKeyWrapper.class);
        QuoteWrapper value = mock(QuoteWrapper.class);

        when(value.getOperation()).thenReturn(operation);
        boolean result = quoteFilter.filter(key, value);

        Assert.assertFalse(result);
        verify(value, times(1)).getOperation();

    }

}