package com.op.ludo.exceptions;

public class BoardNotFoundException extends RuntimeException {

  public BoardNotFoundException(String msg) {
    super(msg);
  }
}
