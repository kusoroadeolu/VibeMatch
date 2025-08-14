package com.victor.VibeMatch.synchandler.services;

import com.victor.VibeMatch.cache.TaskStatus;

public interface TaskService {
    void saveTask(String taskId, TaskStatus taskStatus);

    TaskStatus getTaskStatus(String taskId);

    void evictTask(String taskId);
}
