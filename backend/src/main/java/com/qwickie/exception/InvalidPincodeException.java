package com.qwickie.exception;

/**
 * @author Ankit Sinha
 */
public class InvalidPincodeException extends RuntimeException {
    public InvalidPincodeException(String message) {
        super(message);
    }
}
