package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.synchandler.TaskStatus;
import com.victor.VibeMatch.exceptions.UserSyncException;
import com.victor.VibeMatch.rabbitmq.RabbitSyncConfigProperties;
import com.victor.VibeMatch.synchandler.services.TaskService;
import com.victor.VibeMatch.synchandler.services.UserArtistSyncService;
import com.victor.VibeMatch.synchandler.services.UserTrackSyncService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncOrchestratorImplTest {

    @Mock
    private UserArtistSyncService userArtistSyncService;

    @Mock
    private UserTrackSyncService userTrackSyncService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitSyncConfigProperties rabbitSyncConfigProperties;

    @Mock
    private TaskService taskService;

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private SyncOrchestratorImpl syncOrchestrator;

    private String validTaskId;
    private TaskStatus expectedTaskStatus;
    private User user;

    @BeforeEach
    void setUp() {
        validTaskId = "some-valid-task-id";
        expectedTaskStatus = TaskStatus.SUCCESS;
        user = User.builder().lastSyncedAt(LocalDateTime.now().minusDays(1)).build();
    }

    @Test
    public void shouldSyncAllData(){
        //Arrange
        String spotifyId = "spotify_id";
        User user = User.builder().spotifyId(spotifyId).build();

        //Act
        LocalDateTime syncedAt = syncOrchestrator.syncAllData(user);

        //Assert
        assertNotNull(syncedAt);
        verify(userArtistSyncService, times(1)).syncUserArtist(user);
        verify(userTrackSyncService, times(1)).syncRecentUserTracks(user);
        verify(userTrackSyncService, times(1)).syncTopUserTracks(user);
    }

    @Test
    public void shouldScheduleUserSync(){
        //Arrange
        String spotifyId = "spotify_id";
        user.setSpotifyId(spotifyId);
        TaskStatus status = TaskStatus.PENDING;

        when(taskService.getTaskStatus(spotifyId)).thenReturn(null);
        when(rabbitSyncConfigProperties.getExchangeName()).thenReturn("sync_exchange");
        when(rabbitSyncConfigProperties.getRoutingKey()).thenReturn("sync_key");


        //Act
        String mockTaskId = syncOrchestrator.scheduleUserSync(user);

        //Assert
        assertNotNull(mockTaskId);
        verify(taskService, times(1)).saveTask(mockTaskId, status);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("sync_exchange.dlx"),
                eq("sync_key"),
                eq(spotifyId),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    public void scheduleUserSync_shouldReturnNull_givenPendingStatus(){
        //Arrange
        String spotifyId = "spotify_id";
        user.setSpotifyId(spotifyId);

        when(taskService.getTaskStatus(spotifyId)).thenReturn(TaskStatus.PENDING);


        //Act
        var id = syncOrchestrator.scheduleUserSync(user);
        assertNull(id);

    }

    @Test
    public void scheduleUserSync_shouldReturnNull_ifUserHasSyncedRecently(){
        //Arrange
        String spotifyId = "spotify_id";
        user.setLastSyncedAt(LocalDateTime.now());
        user.setSpotifyId(spotifyId);

        when(taskService.getTaskStatus(spotifyId)).thenReturn(null);

        var id = syncOrchestrator.scheduleUserSync(user);
        assertNull(id);

    }

    @Test
    void getSyncStatus_shouldReturnTaskStatus_givenValidTaskId() {
        // Arrange
        when(taskService.getTaskStatus(eq(validTaskId))).thenReturn(expectedTaskStatus);

        // Act
        TaskStatus result = syncOrchestrator.getSyncStatus(validTaskId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTaskStatus, result);
        verify(taskService, times(1)).getTaskStatus(eq(validTaskId));
    }

    @Test
    void getSyncStatus_shouldThrowUserSyncException_givenNullTaskId() {
        // Arrange
        String nullTaskId = null;

        // Act & Assert
        UserSyncException exception = assertThrows(UserSyncException.class, () -> {
            syncOrchestrator.getSyncStatus(nullTaskId);
        });

        assertEquals("Cannot return sync status for null or empty task ID", exception.getMessage());
        verify(taskService, times(0)).getTaskStatus(any()); // Verify taskService was NOT called
    }

    @Test
    void getSyncStatus_shouldThrowUserSyncException_givenEmptyTaskId() {
        // Arrange
        String emptyTaskId = "";

        // Act & Assert
        UserSyncException exception = assertThrows(UserSyncException.class, () -> {
            syncOrchestrator.getSyncStatus(emptyTaskId);
        });

        assertEquals("Cannot return sync status for null or empty task ID", exception.getMessage());
        verify(taskService, times(0)).getTaskStatus(any()); // Verify taskService was NOT called
    }

    @Test
    void validateSpotifyId_shouldNotThrowException_givenValidSpotifyId() {
        // Arrange
        String validSpotifyId = "valid-spotify-id-123";

        // Act
        // No exception should be thrown
        syncOrchestrator.validateSpotifyId(validSpotifyId);

        // Assert - success if no exception is thrown
    }

    @Test
    void validateSpotifyId_shouldThrowRuntimeException_givenNullSpotifyId() {
        // Arrange
        String nullSpotifyId = null;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            syncOrchestrator.validateSpotifyId(nullSpotifyId);
        });

        assertEquals("Spotify ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void validateSpotifyId_shouldThrowRuntimeException_givenEmptySpotifyId() {
        // Arrange
        String emptySpotifyId = "";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            syncOrchestrator.validateSpotifyId(emptySpotifyId);
        });

        assertEquals("Spotify ID cannot be null or empty", exception.getMessage());
    }

    @Test
    public void hasSyncedRecently_shouldReturnTrue_ifLastSyncWasMoreThan12HoursAgo(){
        //Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = Mockito.mock(User.class);

        //Act
        when(user.getLastSyncedAt()).thenReturn(LocalDateTime.now().minusDays(1));
        boolean hasSyncedPreviously = syncOrchestrator.hasSyncedRecently(now, user);

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
        boolean hasSyncedPreviously = syncOrchestrator.hasSyncedRecently(now, user);

        //Assert
        assertFalse(hasSyncedPreviously);

    }

    @Test
    public void hasSyncedRecently_shouldReturnFalse_ifLastSyncWasLessThan12HoursAgo(){
        //Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = Mockito.mock(User.class);

        //Act
        when(user.getLastSyncedAt()).thenReturn(LocalDateTime.now());
        boolean hasSyncedPreviously = syncOrchestrator.hasSyncedRecently(now, user);

        //assert
        assertTrue(hasSyncedPreviously);

    }
}