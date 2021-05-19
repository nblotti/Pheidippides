package ch.nblotti.Pheidippides.client;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
class ClientDBInfo {

  private String dbUrl;
  private String dbUser;
  private String dbPassword;
}
