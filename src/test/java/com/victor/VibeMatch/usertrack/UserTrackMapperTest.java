package com.victor.VibeMatch.usertrack;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.dtos.UserTrackResponseDto;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTrackMapperTest {

    private UserTrackMapper userTrackMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        userTrackMapper = new UserTrackMapper();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("test_user")
                .build();
    }


    @Test
    void responseDto_shouldMapUserRecentTrackCorrectly() {
        // Arrange
        UserRecentTrack recentTrack = UserRecentTrack.builder()
                .id(UUID.randomUUID())
                .trackSpotifyId("spotify-recent-123")
                .name("Recent Song")
                .artistNames(Set.of("Artist A", "Artist B"))
                .artistIds(Set.of("artistAId", "artistBId"))
                .ranking(1)
                .popularity(85)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        UserTrackResponseDto dto = userTrackMapper.responseDto(recentTrack);

        // Assert
        assertNotNull(dto);
        assertEquals("spotify-recent-123", dto.spotifyId());
        assertEquals("Recent Song", dto.name());
        assertEquals(Set.of("Artist A", "Artist B"), dto.artists());
        assertEquals("test_user", dto.ownedBy());
    }



    @Test
    void responseDto_shouldMapUserTopTrackCorrectly() {
        // Arrange
        UserTopTrack topTrack = UserTopTrack.builder()
                .id(UUID.randomUUID())
                .trackSpotifyId("spotify-top-456")
                .name("Top Hit")
                .artistNames(Set.of("Artist X"))
                .artistIds(Set.of("artistXId"))
                .ranking(10)
                .popularity(99)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        UserTrackResponseDto dto = userTrackMapper.responseDto(topTrack);

        // Assert
        assertNotNull(dto);
        assertEquals("spotify-top-456", dto.spotifyId());
        assertEquals("Top Hit", dto.name());
        assertEquals(Set.of("Artist X"), dto.artists());
        assertEquals("test_user", dto.ownedBy());
    }



    @Test
    void responseDto_shouldThrowIllegalArgumentExceptionForUnsupportedType() {
        // Arrange
        Object unsupportedObject = new Object(); // An object that is neither UserRecentTrack nor UserTopTrack

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userTrackMapper.responseDto(unsupportedObject);
        });

        assertEquals("Passed object is not a type of User Recent Track or User Top Track", thrown.getMessage());
    }



    @Test
    void responseDto_shouldThrowNullPointerExceptionForNullInput() {
        // Arrange
        Object nullObject = null;


        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userTrackMapper.responseDto(nullObject);
        });

        assertEquals("Passed object is not a type of User Recent Track or User Top Track", thrown.getMessage());
    }
}