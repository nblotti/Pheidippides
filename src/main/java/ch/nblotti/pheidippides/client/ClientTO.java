package ch.nblotti.pheidippides.client;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
public class ClientTO {

    @Getter
    private String userName;

    private ClientDBInfo clientDBInfo;

    @Getter
    private List<StrategiesDTO> strategies;

    public String getDbUrl() {
        return clientDBInfo.getDbUrl();
    }

    public String getDbUser() {
        return clientDBInfo.getDbUser();
    }

    public String getDbPassword() {
        return clientDBInfo.getDbPassword();
    }


}



