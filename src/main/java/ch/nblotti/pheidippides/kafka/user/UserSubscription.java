package ch.nblotti.pheidippides.kafka.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscription {

    public List<String> stocks;
    public List<String> etfs;

}
