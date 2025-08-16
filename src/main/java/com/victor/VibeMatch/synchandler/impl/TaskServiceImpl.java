package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.synchandler.Task;
import com.victor.VibeMatch.cache.TaskCacheService;
import com.victor.VibeMatch.synchandler.TaskStatus;
import com.victor.VibeMatch.synchandler.services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskCacheService taskCacheService;

    @Override
    public void saveTask(String taskId, TaskStatus taskStatus){
        Task task = new Task(taskId, taskStatus);
        log.info("Caching task with ID: {}", taskId);

        taskCacheService.saveTask(taskId, task);
        log.info("Successfully cached task with ID: {}", taskId);
    }

    @Override
    public TaskStatus getTaskStatus(String taskId){
        Task task = taskCacheService.getTask(taskId);
        log.info("Successfully retrieved task with ID: {}", task.getTaskId());
        return task.getTaskStatus();
    }

    @Override
    public void evictTask(String taskId){
        taskCacheService.evictTask(taskId);
        log.info("Successfully evicted task with ID: {}", taskId);
    }


}
