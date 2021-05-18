package ch.nblotti.Pheidippides.statemachine;

public enum STATES {
  READY,
  INIT_ZOOKEEPER,
  INIT_DATABASE,
  INIT_STREAMS,
  WAIT_FOR_EVENT,
  ERROR,
  DONE,
  CANCELED;

}
