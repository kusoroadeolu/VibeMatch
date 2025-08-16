package com.victor.VibeMatch.synchandler;

import lombok.Getter;

@Getter
public enum TaskStatus {
    SUCCESS("SUCCESS"),
    PENDING("PENDING"),
    RETRYING("RETRYING"),
    FAIL("FAIL");

    private final String status;

     TaskStatus(String status){
        this.status = status;
    }
}
