package ch.nblotti.pheidippides.kafka.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscription {

    public List<String> stocks =Collections.emptyList();
    public List<String> etfs =Collections.emptyList();



    public void setStocks(List<String> stocks) {
        if (stocks == null) {
            stocks = Collections.emptyList();
        } else {
            this.stocks = stocks;
        }
    }

    public void setEtfs(List<String> etfs) {
        if (etfs == null) {
            etfs = Collections.emptyList();
        } else {
            this.etfs = etfs;
        }
    }

}
