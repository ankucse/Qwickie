package com.qwickie.exception;

public class InvalidPincodeException extends RuntimeException {
    public InvalidPincodeException(String message) {
        super(message);
    }
}
