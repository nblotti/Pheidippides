package ch.nblotti.Pheidippides.zookeeper;

import java.util.List;

public interface ClientListener  {

  public void handleStrategyChange(List<StrategiesDTO> strategiesDTOS);
  public void handleDbInfoChange(String url, String user, String password);

}
