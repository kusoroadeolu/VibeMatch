package com.victor.VibeMatch.exceptions;

public class UserTrackSaveException extends RuntimeException {
  public UserTrackSaveException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserTrackSaveException(String message) {
    super(message);
  }
}
