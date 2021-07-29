package ch.nblotti.pheidippides.kafka.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscription {

    private List<String> stocks = Collections.emptyList();
    private List<String> etfs = Collections.emptyList();


    public void setStocks(List<String> stocks) {
        if (stocks != null) {
            this.stocks = stocks;
        }
    }

    public void setEtfs(List<String> etfs) {
        if (etfs != null) {
            this.etfs = etfs;
        }
    }

}
