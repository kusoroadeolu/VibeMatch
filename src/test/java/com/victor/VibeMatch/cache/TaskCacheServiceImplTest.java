package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.synchandler.Task;
import com.victor.VibeMatch.synchandler.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskCacheServiceImplTest {
    @Autowired
    private TaskCacheServiceImpl cacheService;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;

    private Task task;

    @BeforeEach
    public void setUp(){


        task = new Task(
                "UUID_String",
                TaskStatus.FAIL
        );

        cache = cacheManager.getCache("taskCache");

        if(cache != null){
            cache.clear();
        }
    }

    @Test
    public void saveTask_givenTaskIdAndTask(){
        //Arrange
        String taskId = "mock-id-put";

        //Act
        cacheService.saveTask(taskId, task);

        //Assert
        Task cachedTask = cache.get(taskId, Task.class);
        assertNotNull(cachedTask);
        assertEquals(task.getTaskId(), cachedTask.getTaskId());
        assertEquals(task.getTaskStatus(), cachedTask.getTaskStatus());
    }

    @Test
    public void getTask_shouldReturnTask_onSecondCall(){
        //Arrange
        String taskId = "mock-id-get";
        cache.put(taskId, task);

        //Act
        Task task1 = cacheService.getTask(taskId);
        assertNotNull(cache.get(taskId));


        //Assert
        Task task2 = cache.get(taskId, Task.class);
        assertNotNull(task2);
        assertEquals(task1.getTaskId(), task2.getTaskId());
        assertEquals(task1.getTaskStatus(), task2.getTaskStatus());

    }

    @Test
    public void evictTask_shouldReturnNull_afterEvict(){
        //Arrange
        String taskId = "mock-id-get";
        cacheService.saveTask(taskId, task);
        assertNotNull(cache.get(taskId));

        //Act
        cacheService.evictTask(taskId);

        //Assert
        assertNull(cache.get(taskId));
    }
}