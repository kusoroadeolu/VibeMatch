package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.synchandler.Task;

public interface TaskCacheService {
    Task saveTask(String taskId, Task task);

    Task getTask(String taskId);

    void evictTask(String taskId);
}
