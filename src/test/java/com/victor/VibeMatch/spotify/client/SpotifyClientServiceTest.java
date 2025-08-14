package com.victor.VibeMatch.spotify.client;

import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.spotify.dto.SpotifyTopData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpotifyClientServiceTest<T>{


    private SpotifyClientService spotifyClientService = Mockito.mock(
            SpotifyClientService.class,
            Mockito.CALLS_REAL_METHODS
    );

    @Test
    public void buildUri_shouldReturnUriString_givenQueryParams(){
        //Arrange
        String baseUri = "base_uri";
        String timeRange = "time_range";
        int limit = 5;
        String scope = "scope";

        //Act
        String uri = spotifyClientService.buildUri(baseUri, timeRange, limit, scope);

        //Assert
        assertNotNull(uri);
        assertEquals(String.format("%s?time_range=%s&limit=%s&scope=%s", baseUri,timeRange,limit,scope), uri);

    }

    @Test
    public void buildAuthHeader_givenAccessToken(){
        //Arrange
        String accessToken = "mock_token";

        //Act
        String authHeader = spotifyClientService.buildAuthHeader(accessToken);

        assertNotNull(authHeader);
        assertEquals("Bearer " + accessToken, authHeader);
    }

    @Test
    public void handleSpotifyExceptions_whenTooManyRequests_throwSpotifyRateLimitException(){
        //Arrange
        var entity = new ResponseEntity<>(new SpotifyTopData<T>(), HttpStatus.TOO_MANY_REQUESTS);

        //Act & Assert
        var ex = assertThrows(SpotifyRateLimitException.class, () -> {
            spotifyClientService.handleSpotifyExceptions(entity);
        });
        assertEquals("Spotify rate limit threshold exceeded. Please try again later.", ex.getMessage());
    }

    @Test
    public void handleSpotifyExceptions_whenUnauthorized_throwAuthorizationException(){
        //Arrange
        var entity = new ResponseEntity<>(new SpotifyTopData<T>(), HttpStatus.UNAUTHORIZED);

        //Act & Assert
        var ex = assertThrows(AuthorizationException.class, () -> {
            spotifyClientService.handleSpotifyExceptions(entity);
        });
        assertEquals("Failed to authorize user due to expired access token.", ex.getMessage());
    }

    @Test
    public void buildRequestEntity_shouldReturnHttpEntityWithCorrectAuthHeader() {
        // Arrange
        String accessToken = "mock_token";

        // Act
        HttpEntity<Void> entity = spotifyClientService.buildRequestEntity(accessToken);

        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getHeaders());
        assertTrue(entity.getHeaders().containsKey("Authorization"));
        assertEquals("Bearer " + accessToken, entity.getHeaders().getFirst("Authorization"));
    }



}