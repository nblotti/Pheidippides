package ch.nblotti.pheidippides.statemachine;

public enum EVENTS {
  EVENT_RECEIVED,
  ZK_DB_EVENT_RECEIVED,
  ZK_STRATEGIES_EVENT_RECEIVED,
  EVENT_TREATED,
  SUCCESS,
  ERROR,
  QUIT;
}
