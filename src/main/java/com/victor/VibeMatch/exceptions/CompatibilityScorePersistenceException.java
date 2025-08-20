package com.victor.VibeMatch.exceptions;

public class CompatibilityScorePersistenceException extends RuntimeException {
  public CompatibilityScorePersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public CompatibilityScorePersistenceException(String message) {
        super(message);
    }
}
