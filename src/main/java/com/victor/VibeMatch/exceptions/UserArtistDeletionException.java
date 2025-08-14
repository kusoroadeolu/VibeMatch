package com.victor.VibeMatch.exceptions;

public class UserArtistDeletionException extends RuntimeException {
    public UserArtistDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserArtistDeletionException(String message) {
        super(message);
    }
}
