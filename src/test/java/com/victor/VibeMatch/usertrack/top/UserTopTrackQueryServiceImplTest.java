package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTopTrackQueryServiceImplTest {

    @Mock
    private UserTopTrackRepository userTopTrackRepository;

    @InjectMocks
    private UserTopTrackQueryServiceImpl userTopTrackQueryService;

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
    public void findByUser_shouldReturnTopTracks(){
        //Arrange
        List<UserTopTrack> topTracks = List.of(track1, track2);

        //Act
        when(userTopTrackRepository.findByUser(testUser)).thenReturn(topTracks);

        List<UserTopTrack> mockTracks = userTopTrackQueryService.findByUser(testUser);

        //Assert
        assertAll(
                () -> assertNotNull(mockTracks),
                () -> assertEquals(topTracks, mockTracks)
        );
        verify(userTopTrackRepository, times(1)).findByUser(testUser);
    }

}