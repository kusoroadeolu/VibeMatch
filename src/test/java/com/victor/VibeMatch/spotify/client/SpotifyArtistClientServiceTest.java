package com.victor.VibeMatch.spotify.client;

import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.dto.SpotifyTopData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistClientServiceTest {

    @Mock
    private RestTemplate restTemplate;


    @InjectMocks
    private SpotifyArtistClientService spotifyArtistClientService;

    private SpotifyTopData<SpotifyArtist> topData;

    private ResponseEntity<SpotifyTopData<SpotifyArtist>> entity;

    @BeforeEach
    public void setUp(){
        topData = new SpotifyTopData<>(
                new ArrayList<>(),
                "href",
                5,
                "next",
                5,
                "prev",
                4
        );
        entity = new ResponseEntity<>(topData, HttpStatus.OK);
    }


    @Test
    public void shouldGetTopData(){
        //Arrange
        SpotifyDataRequestDto requestDto = new SpotifyDataRequestDto(
                "test_uri", "medium_term", 10, "top-read"
        );
        String accessToken = "mock_access_token";
        Mockito.when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(entity);

        //Act
        ArrayList<SpotifyArtist> artists = spotifyArtistClientService.getTopData(requestDto, accessToken);

        //Assert
        assertNotNull(artists);
        assertEquals(topData.getItems(), artists);
    }

    @Test
    public void getTopData_givenNullBody_shouldThrowNullPointerException(){
        //Arrange
        SpotifyDataRequestDto requestDto = new SpotifyDataRequestDto(
                "test_uri", "medium_term", 10, "top-read"
        );
        String accessToken = "mock_access_token";
        var mockEntity = new ResponseEntity<>(null, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockEntity);

        //Act & Assert
        var ex = assertThrows(NullPointerException.class, () -> {
            spotifyArtistClientService.getTopData(requestDto, accessToken);
        });
        assertEquals("Received null body from spotify call", ex.getMessage());
    }

    @Test
    public void shouldFetchData(){

        // Arrange
        SpotifyDataRequestDto requestDto = new SpotifyDataRequestDto(
                "test_uri", "medium_term", 10, "top-read"
        );
        String accessToken = "mock_access_token";

        //Act
        Mockito.when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(entity);

        ResponseEntity<SpotifyTopData<SpotifyArtist>> mockEntity = spotifyArtistClientService.fetchData(requestDto, accessToken);

        //Assert
        assertNotNull(mockEntity);
        assertEquals(entity, mockEntity);
        assertEquals(topData, mockEntity.getBody());

    }

}