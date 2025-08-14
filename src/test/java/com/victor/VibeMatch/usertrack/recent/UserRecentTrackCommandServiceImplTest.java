package com.victor.VibeMatch.usertrack.recent;

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
class UserRecentTrackCommandServiceImplTest {

    @Mock
    private UserRecentTrackRepository userRecentTrackRepository;

    @InjectMocks
    private UserRecentTrackCommandServiceImpl userRecentTrackCommandService;

    private User testUser;
    private UserRecentTrack track1;
    private UserRecentTrack track2;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID()).build();

        track1 = UserRecentTrack.builder()
                .user(testUser)
                .trackSpotifyId("track1-id")
                .name("Track One")
                .artistNames(Collections.singleton("Artist One"))
                .ranking(1)
                .popularity(80)
                .build();

        track2 = UserRecentTrack.builder()
                .user(testUser)
                .trackSpotifyId("track2-id")
                .name("Track Two")
                .artistNames(Collections.singleton("Artist Two"))
                .ranking(2)
                .popularity(75)
                .build();
    }

    @Test
    void saveRecentTracks_shouldSaveSuccessfully() {
        // Arrange
        List<UserRecentTrack> tracksToSave = List.of(track1, track2);
        when(userRecentTrackRepository.saveAll(tracksToSave)).thenReturn(tracksToSave);

        // Act
        List<UserRecentTrack> savedTracks = userRecentTrackCommandService.saveRecentTracks(tracksToSave);

        // Assert
        assertNotNull(savedTracks);
        assertEquals(2, savedTracks.size());
        verify(userRecentTrackRepository, times(1)).saveAll(tracksToSave);
    }

    @Test
    void saveRecentTracks_shouldHandleEmptyList() {
        // Arrange
        List<UserRecentTrack> tracksToSave = Collections.emptyList();

        // Act
        List<UserRecentTrack> savedTracks = userRecentTrackCommandService.saveRecentTracks(tracksToSave);

        // Assert
        assertTrue(savedTracks.isEmpty());
        verify(userRecentTrackRepository, never()).saveAll(any());
    }

    @Test
    void saveRecentTracks_shouldHandleNullList() {
        // Act
        List<UserRecentTrack> savedTracks = userRecentTrackCommandService.saveRecentTracks(null);

        // Assert
        assertTrue(savedTracks.isEmpty());
        verify(userRecentTrackRepository, never()).saveAll(any());
    }

    @Test
    void saveRecentTracks_shouldThrowExceptionOnDataIntegrityViolation() {
        // Arrange
        List<UserRecentTrack> tracksToSave = List.of(track1);
        when(userRecentTrackRepository.saveAll(tracksToSave)).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(UserTrackSaveException.class, () -> userRecentTrackCommandService.saveRecentTracks(tracksToSave));
    }

    @Test
    void saveRecentTracks_shouldThrowExceptionOnUnexpectedError() {
        // Arrange
        List<UserRecentTrack> tracksToSave = List.of(track1);
        when(userRecentTrackRepository.saveAll(tracksToSave)).thenThrow(new RuntimeException("Database down"));

        // Act & Assert
        assertThrows(UserTrackSaveException.class, () -> userRecentTrackCommandService.saveRecentTracks(tracksToSave));
    }



    @Test
    void deleteAllRecentTracksByUser_shouldDeleteSuccessfully() {
        // Arrange
        when(userRecentTrackRepository.deleteByUser(testUser)).thenReturn(2);

        // Act & Assert
        assertDoesNotThrow(() -> userRecentTrackCommandService.deleteAllRecentTracksByUser(testUser));
        verify(userRecentTrackRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    void deleteAllRecentTracksByUser_shouldHandleNoTracksFound() {
        // Arrange
        when(userRecentTrackRepository.deleteByUser(testUser)).thenReturn(0);

        // Act & Assert
        assertDoesNotThrow(() -> userRecentTrackCommandService.deleteAllRecentTracksByUser(testUser));
        verify(userRecentTrackRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    void deleteAllRecentTracksByUser_shouldThrowExceptionOnNullUser() {
        // Act & Assert
        assertThrows(UserTrackDeletionException.class, () -> userRecentTrackCommandService.deleteAllRecentTracksByUser(null));
        verify(userRecentTrackRepository, never()).deleteByUser(any());
    }

    @Test
    void deleteAllRecentTracksByUser_shouldThrowExceptionOnRepositoryError() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(userRecentTrackRepository).deleteByUser(testUser);

        // Act & Assert
        assertThrows(UserTrackDeletionException.class, () -> userRecentTrackCommandService.deleteAllRecentTracksByUser(testUser));
    }
}