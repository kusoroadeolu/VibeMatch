package com.victor.VibeMatch.exceptions;

public class ConnectionPersistenceException extends RuntimeException {

    public ConnectionPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionPersistenceException(String message) {
        super(message);
    }
}
