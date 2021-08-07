package com.op.ludo.exceptions;

public class CoinServiceException extends RuntimeException {

    public CoinServiceException(String msg, Exception e) {
        super(msg, e);
    }
}
