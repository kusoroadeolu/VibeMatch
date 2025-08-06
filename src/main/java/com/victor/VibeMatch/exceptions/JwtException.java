package com.victor.VibeMatch.exceptions;

public class JwtException extends RuntimeException {

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtException(String message) {
        super(message);
    }
}
