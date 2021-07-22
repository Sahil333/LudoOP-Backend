package com.op.ludo.exceptions;

public class GameStartException extends RuntimeException {

  public GameStartException(String msg, Throwable e) {
    super(msg, e);
  }
}
