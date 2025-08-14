package com.victor.VibeMatch.spotify.factory;

import com.victor.VibeMatch.spotify.DataServiceEnum;
import com.victor.VibeMatch.spotify.SpotifyDataService;
import com.victor.VibeMatch.spotify.client.SpotifyArtistClientService;
import com.victor.VibeMatch.spotify.client.SpotifyTrackClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpotifyDataFactory{

    private final SpotifyArtistClientService artistClientService;
    private final SpotifyTrackClientService trackClientService;


    //Factory to inject either service based on the enum given
    public <T>SpotifyDataService<T> createService(DataServiceEnum serviceType){
        switch (serviceType){
            case TRACKS -> {
                return (SpotifyDataService<T>) trackClientService;
            }
            case ARTISTS -> {
                return (SpotifyDataService<T>) artistClientService;
            }
            default -> throw new NullPointerException("Null or unknown service type: " + serviceType);
        }

    }

}



