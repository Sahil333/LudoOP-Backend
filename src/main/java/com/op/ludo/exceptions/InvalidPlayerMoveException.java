package com.op.ludo.exceptions;

public class InvalidPlayerMoveException extends RuntimeException {
    public InvalidPlayerMoveException(String msg) {
        super(msg);
    }
}
