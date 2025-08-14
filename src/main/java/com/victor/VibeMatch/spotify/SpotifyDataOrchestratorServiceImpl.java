package com.victor.VibeMatch.spotify;

import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.factory.SpotifyDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;

@Slf4j
@RequiredArgsConstructor
@Service
public class SpotifyDataOrchestratorServiceImpl<T> implements SpotifyDataOrchestratorService<T> {
    private final SpotifyDataFactory spotifyDataFactory;


    /**
     * Fetches spotify data based on the enum type given
     * @param serviceType The enum required to know which service to create
     * @param accessToken The access token
     * @param request The dto which contains needed info to fetch data
     * @return An unknown list of the data required
     * */
    @Override
    public <T> ArrayList<T> fetchSpotifyData(DataServiceEnum serviceType, SpotifyDataRequestDto request, String accessToken) {
        var service = spotifyDataFactory.createService(serviceType);
        return service.getTopData(request, accessToken);
    }


}
