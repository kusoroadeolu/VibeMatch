package com.victor.VibeMatch.exceptions;

public class UserArtistSaveException extends RuntimeException {
    public UserArtistSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserArtistSaveException(String message) {
        super(message);
    }
}
