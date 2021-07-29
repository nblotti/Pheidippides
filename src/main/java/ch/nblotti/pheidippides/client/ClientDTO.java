package ch.nblotti.pheidippides.client;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor

public class ClientDTO {

    @Getter
    private String userName;

    private ClientDBInfo clientDBInfo;

    @Getter
    private List<StrategiesDTO> strategies;

    @GeneratedExcludeJacocoTestCoverage
    public String getDbUrl() {
        return clientDBInfo.getDbUrl();
    }

    @GeneratedExcludeJacocoTestCoverage
    public String getDbUser() {
        return clientDBInfo.getDbUser();
    }

    @GeneratedExcludeJacocoTestCoverage
    public String getDbPassword() {
        return clientDBInfo.getDbPassword();
    }


}



