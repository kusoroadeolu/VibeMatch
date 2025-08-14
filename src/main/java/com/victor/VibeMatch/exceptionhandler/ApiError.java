package com.victor.VibeMatch.exceptionhandler;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ApiError {
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
}
