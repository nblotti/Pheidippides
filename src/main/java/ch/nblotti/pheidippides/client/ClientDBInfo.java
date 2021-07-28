package ch.nblotti.pheidippides.client;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class ClientDBInfo {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
}
