package com.victor.VibeMatch.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public interface TaskCacheService {
    Task saveTask(String taskId, Task task);

    Task getTask(String taskId);

    void evictTask(String taskId);
}
