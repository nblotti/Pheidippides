package ch.nblotti.Pheidippides.statemachine;

import ch.nblotti.Pheidippides.zookeeper.ClientListener;
import ch.nblotti.Pheidippides.zookeeper.StrategiesDTO;
import ch.nblotti.Pheidippides.zookeeper.ZooKeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

import java.util.List;

@WithStateMachine
@Slf4j
public class PheidippidesStateMachineListener {

  private final ZooKeeperService zooKeeperService;

  @Autowired
  public PheidippidesStateMachineListener(ZooKeeperService zooKeeperService) {
    this.zooKeeperService = zooKeeperService;
  }

  @StatesOnTransition(source = STATES.READY, target = STATES.INIT_ZOOKEEPER)
  public void fromReadyToFreeClient() {

    zooKeeperService.subscribe(new ClientListener() {
      @Override
      public void handleStrategyChange(List<StrategiesDTO> strategiesDTOS) {
        strategiesDTOS.stream().forEach(i -> log.info(String.format("Client id : %s - Strategy name : %s", i.getClientName(), i.getStrategyName())));
      }

      @Override
      public void handleDbInfoChange(String url, String user, String password) {
        log.info(String.format("DB url : %s - User : %s - Password : %s", url,user,password));
      }
    });

  }
  @StatesOnTransition(source = STATES.INIT_ZOOKEEPER, target = STATES.INIT_DATABASE)
  public void fromFreeClientToRegisterNode() {

  }

  @StatesOnTransition(source = STATES.INIT_DATABASE, target = STATES.INIT_STREAMS)
  public void fromRegisterNodeToReadClientData() {

  }

  @StatesOnTransition(source = STATES.INIT_STREAMS, target = STATES.DONE)
  public void fromReadClientDataToConnectDB() {

  }

  @OnTransition
  public void toError() {
  }

}
