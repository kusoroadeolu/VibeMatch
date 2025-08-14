package com.victor.VibeMatch.exceptions;

public class UserTrackDeletionException extends RuntimeException {
  public UserTrackDeletionException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserTrackDeletionException(String message) {
    super(message);
  }
}
