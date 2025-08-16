package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.synchandler.Task;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TaskCacheServiceImpl implements TaskCacheService {

    @CachePut(key = "#taskId", cacheNames = "taskCache")
    @Override
    public Task saveTask(String taskId, Task task){
        return task;
    }

    @Cacheable(key = "#taskId", cacheNames = "taskCache")
    @Override
    public Task getTask(String taskId){
        return new Task();
    }

    @CacheEvict(key = "#taskId", cacheNames = "taskCache")
    @Override
    public void evictTask(String taskId){

    }
}
