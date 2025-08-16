package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.synchandler.Task;
import com.victor.VibeMatch.cache.TaskCacheService;
import com.victor.VibeMatch.synchandler.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskCacheService taskCacheService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private String taskId;
    private TaskStatus taskStatus;
    private Task mockTask;

    @BeforeEach
    void setUp() {
        taskId = "test-task-id";
        taskStatus = TaskStatus.SUCCESS;
        mockTask = new Task(taskId, taskStatus);
    }

    @Test
    void saveTask_shouldCallCacheServiceSaveTask() {
        // Act
        taskService.saveTask(taskId, taskStatus);

        // Assert
        verify(taskCacheService, times(1)).saveTask(eq(taskId), any(Task.class));
    }

    @Test
    void getTaskStatus_shouldCallCacheServiceGetTaskAndReturnStatus() {
        // Arrange
        when(taskCacheService.getTask(taskId)).thenReturn(mockTask);

        // Act
        TaskStatus resultStatus = taskService.getTaskStatus(taskId);

        // Assert
        verify(taskCacheService, times(1)).getTask(eq(taskId));
        assertEquals(taskStatus, resultStatus);
    }

    @Test
    void evictTask_shouldCallCacheServiceEvictTask() {
        // Act
        taskService.evictTask(taskId);

        // Assert
        verify(taskCacheService, times(1)).evictTask(eq(taskId));
    }
}