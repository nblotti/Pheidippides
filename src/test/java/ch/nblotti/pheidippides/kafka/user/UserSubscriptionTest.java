package ch.nblotti.pheidippides.kafka.user;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionTest {


    @Test
    void setStocks() {

        List<String> stocks = mock(List.class);
        UserSubscription userSubscription = new UserSubscription();

        userSubscription.setStocks(stocks);
        Assert.assertEquals(userSubscription.getStocks(),stocks);
    }

    @Test
    void setEtfs() {

        List<String> etfs = mock(List.class);
        UserSubscription userSubscription = new UserSubscription();

        userSubscription.setEtfs(etfs);
        Assert.assertEquals(userSubscription.getEtfs(), etfs);
    }


    @Test
    void setNullStocks() {
        UserSubscription userSubscription = new UserSubscription();

        userSubscription.setStocks(null);
        Assert.assertEquals(Collections.emptyList(),userSubscription.getStocks());
    }

    @Test
    void setNullEtfs() {

        UserSubscription userSubscription = new UserSubscription();

        userSubscription.setEtfs(null);
        Assert.assertEquals(Collections.emptyList(),userSubscription.getEtfs());
    }
}