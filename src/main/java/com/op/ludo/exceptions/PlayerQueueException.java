package com.op.ludo.exceptions;

public class PlayerQueueException extends RuntimeException {
  public PlayerQueueException(String msg, Throwable e) {
    super(msg, e);
  }
}
