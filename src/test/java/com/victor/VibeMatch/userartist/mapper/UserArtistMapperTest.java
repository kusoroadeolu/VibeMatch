package com.victor.VibeMatch.userartist.mapper;

import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.userartist.UserArtistResponseDto;
import com.victor.VibeMatch.user.User; // Assuming User class is in this package
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserArtistMapperTest {

    @InjectMocks
    private UserArtistMapper userArtistMapper;

    @Mock
    private UserArtist userArtist;

    @Mock
    private User user;

    @Mock
    private SpotifyArtist spotifyArtist;

    @Test
    void testResponseDto() {
        // Given
        String artistSpotifyId = "123XYZ";
        String name = "test_artist";
        int popularity = 85;
        Set<String> genres = Set.of("pop", "rock");
        int ranking = 1;
        String username = "test_user";

        // Define behavior for the mocked UserArtist and User objects
        when(userArtist.getArtistSpotifyId()).thenReturn(artistSpotifyId);
        when(userArtist.getName()).thenReturn(name);
        when(userArtist.getPopularity()).thenReturn(popularity);
        when(userArtist.getGenres()).thenReturn(genres);
        when(userArtist.getRanking()).thenReturn(ranking);
        when(userArtist.getUser()).thenReturn(user); // Return the mocked User object
        when(user.getUsername()).thenReturn(username); // Return the username from the mocked User

        // When
        UserArtistResponseDto resultDto = userArtistMapper.responseDto(userArtist);

        // Then
        // Assert that the returned DTO matches the expected values
        assertEquals(artistSpotifyId, resultDto.id());
        assertEquals(name, resultDto.name());
        assertEquals(popularity, resultDto.popularity());
        assertEquals(genres, resultDto.genres());
        assertEquals(ranking, resultDto.rank());
        assertEquals(username, userArtist.getUser().getUsername());

        // You can also assert the entire DTO object if it has an equals and hashCode method overridden
        UserArtistResponseDto expectedDto = new UserArtistResponseDto(
                artistSpotifyId,
                name,
                popularity,
                genres,
                ranking,
                username
        );
        assertEquals(expectedDto, resultDto);
    }

    @Test
    void testBuildUserArtist() {
        // Given
        String testUserUsername = "test_user123";
        String spotifyArtistId = "spotifyId_XYZ";
        String spotifyArtistName = "Acoustic Wonders";
        int spotifyArtistRank = 5;
        int spotifyArtistPopularity = 90;
        Set<String> spotifyArtistGenres = new HashSet<>(Arrays.asList("acoustic", "folk", "indie"));

        when(spotifyArtist.getId()).thenReturn(spotifyArtistId);
        when(spotifyArtist.getName()).thenReturn(spotifyArtistName);
        when(spotifyArtist.getRank()).thenReturn(spotifyArtistRank);
        when(spotifyArtist.getPopularity()).thenReturn(spotifyArtistPopularity);
        when(spotifyArtist.getGenres()).thenReturn(spotifyArtistGenres);

        // When
        UserArtist resultUserArtist = userArtistMapper.buildUserArtist(user, spotifyArtist);

        // Then
        assertNotNull(resultUserArtist);
        assertEquals(user, resultUserArtist.getUser()); // Should be the same mocked user object
        assertEquals(spotifyArtistId, resultUserArtist.getArtistSpotifyId());
        assertEquals(spotifyArtistName, resultUserArtist.getName());
        assertEquals(spotifyArtistRank, resultUserArtist.getRanking());
        assertEquals(spotifyArtistPopularity, resultUserArtist.getPopularity());
        assertEquals(spotifyArtistGenres, resultUserArtist.getGenres());

    }
}
