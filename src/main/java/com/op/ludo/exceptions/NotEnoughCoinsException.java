package com.op.ludo.exceptions;

public class NotEnoughCoinsException extends RuntimeException {

    public NotEnoughCoinsException(String msg) {
        super(msg);
    }
}
