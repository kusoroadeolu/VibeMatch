package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.exceptions.NoSuchUserException;
import com.victor.VibeMatch.exceptions.UserTrackDeletionException;
import com.victor.VibeMatch.exceptions.UserTrackSaveException;
import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTopTrackCommandServiceImplTest {

    @Mock
    private UserTopTrackRepository userTopTrackRepository;

    @InjectMocks
    private UserTopTrackCommandServiceImpl userTopTrackCommandService;

    private User testUser;
    private UserTopTrack track1;
    private UserTopTrack track2;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID()).build();

        track1 = UserTopTrack.builder()
                .user(testUser)
                .trackSpotifyId("track1-id")
                .name("Top Track One")
                .artistNames(Collections.singleton("Artist One"))
                .ranking(1)
                .popularity(95)
                .build();

        track2 = UserTopTrack.builder()
                .user(testUser)
                .trackSpotifyId("track2-id")
                .name("Top Track Two")
                .artistNames(Collections.singleton("Artist Two"))
                .ranking(2)
                .popularity(90)
                .build();
    }



    @Test
    void saveTopTracks_shouldSaveSuccessfully() {
        // Arrange
        List<UserTopTrack> tracksToSave = List.of(track1, track2);
        when(userTopTrackRepository.saveAll(tracksToSave)).thenReturn(tracksToSave);

        // Act
        List<UserTopTrack> savedTracks = userTopTrackCommandService.saveTopTracks(tracksToSave);

        // Assert
        assertNotNull(savedTracks);
        assertEquals(2, savedTracks.size());
        verify(userTopTrackRepository, times(1)).saveAll(tracksToSave);
    }

    @Test
    void saveTopTracks_shouldHandleEmptyList() {
        // Arrange
        List<UserTopTrack> tracksToSave = Collections.emptyList();

        // Act
        List<UserTopTrack> savedTracks = userTopTrackCommandService.saveTopTracks(tracksToSave);

        // Assert
        assertTrue(savedTracks.isEmpty());
        verify(userTopTrackRepository, never()).saveAll(any());
    }

    @Test
    void saveTopTracks_shouldHandleNullList() {
        // Act
        List<UserTopTrack> savedTracks = userTopTrackCommandService.saveTopTracks(null);

        // Assert
        assertTrue(savedTracks.isEmpty());
        verify(userTopTrackRepository, never()).saveAll(any());
    }

    @Test
    void saveTopTracks_shouldThrowExceptionOnDataIntegrityViolation() {
        // Arrange
        List<UserTopTrack> tracksToSave = List.of(track1);
        when(userTopTrackRepository.saveAll(tracksToSave)).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(UserTrackSaveException.class, () -> userTopTrackCommandService.saveTopTracks(tracksToSave));
    }

    @Test
    void saveTopTracks_shouldThrowExceptionOnUnexpectedError() {
        // Arrange
        List<UserTopTrack> tracksToSave = List.of(track1);
        when(userTopTrackRepository.saveAll(tracksToSave)).thenThrow(new RuntimeException("Database down"));

        // Act & Assert
        assertThrows(UserTrackSaveException.class, () -> userTopTrackCommandService.saveTopTracks(tracksToSave));
    }



    @Test
    void deleteAllTopTracks_shouldDeleteSuccessfullyByUser() {
        // Arrange
        when(userTopTrackRepository.deleteByUser(testUser)).thenReturn(2);

        // Act
        userTopTrackCommandService.deleteAllTopTracksByUser(testUser);

        // Assert
        verify(userTopTrackRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    void deleteAllTopTracks_shouldHandleNoTracksByUserFound() {
        // Arrange
        when(userTopTrackRepository.deleteByUser(testUser)).thenReturn(0);

        // Act
        userTopTrackCommandService.deleteAllTopTracksByUser(testUser);

        // Assert
        verify(userTopTrackRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    void deleteAllTopTracks_ByUser_shouldThrowExceptionOnNullUser() {
        // Act & Assert
        assertThrows(NoSuchUserException.class, () -> userTopTrackCommandService.deleteAllTopTracksByUser(null));
        verify(userTopTrackRepository, never()).deleteByUser(any());
    }

    @Test
    void deleteAllTopTracks_ByUser_shouldThrowExceptionOnRepositoryError() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(userTopTrackRepository).deleteByUser(testUser);

        // Act & Assert
        assertThrows(UserTrackDeletionException.class, () -> userTopTrackCommandService.deleteAllTopTracksByUser(testUser));
    }
}