package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRecentTrackQueryServiceImplTest {


    @Mock
    private UserRecentTrackRepository recentTrackRepository;

    @InjectMocks
    private UserRecentTrackQueryServiceImpl recentTrackQueryService;

    private UserRecentTrack track1;
    private User testUser;


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
    }

    @Test
    public void findByUser_shouldReturnFoundTracks(){
        //Arrange
        List<UserRecentTrack> recentTracks = List.of(track1);

        //Act
        when(recentTrackRepository.findByUser(testUser)).thenReturn(recentTracks);

        List<UserRecentTrack> mockTracks = recentTrackQueryService.findByUser(testUser);

        //Assert
        assertNotNull(mockTracks);
        assertEquals(1, mockTracks.size());
        assertEquals(recentTracks.getFirst(), mockTracks.getFirst());
        verify(recentTrackRepository, times(1)).findByUser(testUser);
    }

    @Test
    public void existsByUser_shouldReturnTrueIfUserExists(){
        //Arrange
        when(recentTrackRepository.existsByUser(testUser)).thenReturn(true);

        //Act
        boolean exists = recentTrackQueryService.existsByUser(testUser);

        //Assert
        assertTrue(exists);
    }

    @Test
    public void existsByUser_shouldReturnFalseIfUserDoesNotExist(){
        //Arrange
        when(recentTrackRepository.existsByUser(testUser)).thenReturn(false);

        //Act
        boolean exists = recentTrackQueryService.existsByUser(testUser);

        //Assert
        assertFalse(exists);
    }
}