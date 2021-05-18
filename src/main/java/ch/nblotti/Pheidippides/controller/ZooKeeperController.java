package ch.nblotti.Pheidippides.controller;

import ch.nblotti.Pheidippides.statemachine.EVENTS;
import ch.nblotti.Pheidippides.statemachine.STATES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZooKeeperController {

  @Autowired
  StateMachine<STATES, EVENTS> stateMachine;


  @GetMapping("/")
  public void startMachine() {

    stateMachine.sendEvent(EVENTS.EVENT_RECEIVED);
  }


}
