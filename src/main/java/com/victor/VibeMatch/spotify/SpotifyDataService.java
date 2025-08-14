package com.victor.VibeMatch.spotify;


import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;

import java.util.ArrayList;


public interface SpotifyDataService<T> {
    <T>ArrayList<T> getTopData(SpotifyDataRequestDto request, String accessToken)
            throws SpotifyRateLimitException, AuthorizationException;
}
