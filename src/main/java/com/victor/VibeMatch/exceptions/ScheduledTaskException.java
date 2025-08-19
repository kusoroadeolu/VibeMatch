package com.victor.VibeMatch.exceptions;

public class ScheduledTaskException extends RuntimeException {
    public ScheduledTaskException(String message) {
        super(message);
    }

    public ScheduledTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
