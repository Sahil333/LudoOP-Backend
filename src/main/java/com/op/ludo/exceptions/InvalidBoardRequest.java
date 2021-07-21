package com.op.ludo.exceptions;

public class InvalidBoardRequest extends RuntimeException {

  public InvalidBoardRequest(String msg) {
    super(msg);
  }
}
