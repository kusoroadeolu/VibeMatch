package com.victor.VibeMatch.exceptions;

import lombok.Getter;

@Getter
public class SpotifyRateLimitException extends RuntimeException {
    public SpotifyRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public SpotifyRateLimitException(String message) {
        super(message);
    }

    private long retryAfterSeconds;


}
