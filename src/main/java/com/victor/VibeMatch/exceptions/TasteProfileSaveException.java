package com.victor.VibeMatch.exceptions;

public class TasteProfileSaveException extends RuntimeException {
    public TasteProfileSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public TasteProfileSaveException(String message) {
        super(message);
    }
}
