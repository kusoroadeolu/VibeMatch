package com.victor.VibeMatch.spotify;

import com.victor.VibeMatch.spotify.client.SpotifyArtistClientService;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.factory.SpotifyDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.victor.VibeMatch.spotify.DataServiceEnum.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyDataOrchestratorServiceImplTest {
    @Mock
    private SpotifyDataFactory spotifyDataFactory;

    @Mock
    private SpotifyDataService<Object> mockSpotifyDataService;

    @InjectMocks
    private SpotifyDataOrchestratorServiceImpl<Object> spotifyDataOrchestratorService;

    private SpotifyDataRequestDto requestDto;

    private String accessToken;

    @BeforeEach
    public void setUp(){

       requestDto = new SpotifyDataRequestDto(
                "test_uri", "medium_term", 10, "top-read"
        );

       accessToken = "mock_token";
    }

    @Test
    public void fetchSpotifyData_shouldReturnSpotifyTrackList_givenArtistEnum(){
        //Arrange
        var list = new ArrayList<Object>(List.of("Artist1", "Artist2"));


        //Act
        when(spotifyDataFactory.createService(ARTISTS)).thenReturn(mockSpotifyDataService);
        when(mockSpotifyDataService.getTopData(requestDto, accessToken)).thenReturn(list);

        ArrayList<Object> mockList = spotifyDataOrchestratorService.fetchSpotifyData(ARTISTS, requestDto, accessToken);

        //Assert
        assertNotNull(mockList);
        assertEquals(list, mockList);
        verify(spotifyDataFactory, times(1)).createService(ARTISTS);
        verify(mockSpotifyDataService, times(1)).getTopData(requestDto, accessToken);

    }
}