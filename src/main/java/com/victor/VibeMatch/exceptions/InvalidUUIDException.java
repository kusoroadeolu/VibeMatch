package com.victor.VibeMatch.exceptions;

public class InvalidUUIDException extends RuntimeException {
    public InvalidUUIDException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUUIDException(String message) {
        super(message);
    }
}
