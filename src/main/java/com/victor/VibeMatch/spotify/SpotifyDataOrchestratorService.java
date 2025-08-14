package com.victor.VibeMatch.spotify;

import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;

import java.util.ArrayList;

public interface SpotifyDataOrchestratorService<T> {
    <T> ArrayList<T> fetchSpotifyData(DataServiceEnum serviceType, SpotifyDataRequestDto request, String accessToken);


}
