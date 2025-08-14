package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.cache.TaskStatus;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import com.victor.VibeMatch.synchandler.services.TaskService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserCommandService;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncListenerTest {

    @InjectMocks
    private SyncListener syncListener;

    @Test
    public void hasSyncedPreviously_shouldReturnTrue_ifLastSyncWasMoreThan12HoursAgo(){
        //Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = Mockito.mock(User.class);

        //Act
        when(user.getLastSyncedAt()).thenReturn(LocalDateTime.now().minusDays(1));
        boolean hasSyncedPreviously = syncListener.hasSyncedPreviously(now, user);

        //Assert
        assertFalse(hasSyncedPreviously);

    }

    @Test
    public void hasSyncedPreviously_shouldReturnTrue_ifLastSyncedAtIsNull(){
        //Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = Mockito.mock(User.class);

        //Act
        when(user.getLastSyncedAt()).thenReturn(null);
        boolean hasSyncedPreviously = syncListener.hasSyncedPreviously(now, user);

        //Assert
        assertFalse(hasSyncedPreviously);

    }

    @Test
    public void hasSyncedPreviously_shouldReturnFalse_ifLastSyncWasLessThan12HoursAgo(){
        //Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = Mockito.mock(User.class);

        //Act
        when(user.getLastSyncedAt()).thenReturn(LocalDateTime.now());
        boolean hasSyncedPreviously = syncListener.hasSyncedPreviously(now, user);

        //assert
        assertTrue(hasSyncedPreviously);

    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    public class InnerClass{
        @Mock
        private UserQueryService userQueryService;
        @Mock
        private UserCommandService userCommandService;
        @Mock
        private TaskService taskService;
        @Mock
        private RabbitTemplate rabbitTemplate;
        @Mock
        private SyncOrchestrator syncOrchestrator;

        @Mock
        private Message message;

        @InjectMocks
        private SyncListener syncListener;

        @Mock
        private MessageProperties messageProperties;

        String spotifyId;
        String taskId;
        User user;

        @BeforeEach
        public void setUp(){
            spotifyId = "spotify_id";
            taskId = "task_id";
            user = User.builder().lastSyncedAt(LocalDateTime.now().minusDays(1)).build();
            when(message.getBody()).thenReturn(spotifyId.getBytes());
            when(messageProperties.getHeader(eq("taskId"))).thenReturn(taskId);
            when(message.getMessageProperties()).thenReturn(messageProperties);
        }


        @Test
        public void shouldQueueUsers() throws IOException {
            //Act
            when(userQueryService.findBySpotifyId(spotifyId)).thenReturn(user);
            syncListener.queueUsers(message);

            //Assert
            verify(syncOrchestrator, times(1)).syncAllData(spotifyId, user);
            verify(userCommandService, times(1)).saveUser(user);
            verify(taskService, times(1)).saveTask(taskId, TaskStatus.SUCCESS);
        }

        @Test
        public void queueUsers_shouldThrowAmqpEx_onRateLimitEx() throws IOException {
            //Arrange
            when(messageProperties.getReceivedRoutingKey()).thenReturn(anyString());
            when(userQueryService.findBySpotifyId(spotifyId)).thenReturn(user);

            //Act
            doThrow(new SpotifyRateLimitException("", 50)).when(syncOrchestrator).syncAllData(spotifyId, user);

            //Assert
            assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
                when(userQueryService.findBySpotifyId(spotifyId)).thenReturn(user);
                syncListener.queueUsers(message);
            });
            verify(taskService, times(1)).saveTask(taskId, TaskStatus.RETRYING);
            verify(rabbitTemplate, times(1)).convertAndSend(
                    eq("sync-exchange.dlx"),
                    anyString(),
                    anyString(),
                    any(MessagePostProcessor.class)
            );
            verify(userCommandService, never()).saveUser(user);
        }

        @Test
        public void queueUsers_shouldThrowAmqpException_onGenericException() throws IOException {
            //Arrange
            doThrow(new RuntimeException("Generic failure")).when(syncOrchestrator).syncAllData(spotifyId, user);

            //Act & Assert
            assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
                when(userQueryService.findBySpotifyId(spotifyId)).thenReturn(user);
                syncListener.queueUsers(message);
            });
            verify(userCommandService, never()).saveUser(user);
        }
    }

}