package com.op.ludo.exceptions;

public class GameException extends RuntimeException {

  public GameException(String msg, Throwable e) {
    super(msg, e);
  }

  public GameException(String msg) {
    super(msg);
  }

  public GameException(Throwable e) {
    super(e);
  }
}
