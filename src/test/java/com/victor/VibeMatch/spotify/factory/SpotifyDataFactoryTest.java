package com.victor.VibeMatch.spotify.factory;

import com.victor.VibeMatch.spotify.DataServiceEnum;
import com.victor.VibeMatch.spotify.SpotifyDataService;
import com.victor.VibeMatch.spotify.client.SpotifyArtistClientService;
import com.victor.VibeMatch.spotify.client.SpotifyTrackClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.victor.VibeMatch.spotify.DataServiceEnum.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpotifyDataFactoryTest {
    @Mock
    private SpotifyArtistClientService artistClientService;

    @Mock
    private SpotifyTrackClientService trackClientService;

    @InjectMocks
    private SpotifyDataFactory spotifyDataFactory;

    @Test
    public void createService_shouldReturnArtistService_givenArtistEnum(){


        SpotifyDataService<Object> mockService = spotifyDataFactory.createService(ARTISTS);

        //Assert
        assertNotNull(mockService);
        assertEquals(artistClientService, mockService);
    }

    @Test
    public void createService_shouldReturnTrackService_givenTrackEnum(){
        //Act
        SpotifyDataService<Object> mockService = spotifyDataFactory.createService(TRACKS);

        //Assert
        assertNotNull(mockService);
        assertEquals(trackClientService, mockService);
    }

    @Test
    public void createService_shouldThrowNullPointerException_givenInvalidEnum(){
        //Act & Assert
         assertThrows(NullPointerException.class, () -> {
            spotifyDataFactory.createService(null);
        });
    }
}