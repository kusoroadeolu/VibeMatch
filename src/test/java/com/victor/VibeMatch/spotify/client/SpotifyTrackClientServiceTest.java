package com.victor.VibeMatch.spotify.client;

import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.dto.SpotifyTopData;
import com.victor.VibeMatch.spotify.dto.SpotifyTrack;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SpotifyTrackClientServiceTest {

    @Mock
    private RestTemplate restTemplate;


    @InjectMocks
    private SpotifyTrackClientService spotifyTrackClientService;

    private SpotifyTopData<SpotifyTrack> topData;

    private ResponseEntity<SpotifyTopData<SpotifyTrack>> entity;

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
        ArrayList<SpotifyTrack> artists = spotifyTrackClientService.getTopData(requestDto, accessToken);

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
            spotifyTrackClientService.getTopData(requestDto, accessToken);
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

        ResponseEntity<SpotifyTopData<SpotifyTrack>> mockEntity = spotifyTrackClientService.fetchData(requestDto, accessToken);

        //Assert
        assertNotNull(mockEntity);
        assertEquals(entity, mockEntity);
        assertEquals(topData, mockEntity.getBody());

    }

}