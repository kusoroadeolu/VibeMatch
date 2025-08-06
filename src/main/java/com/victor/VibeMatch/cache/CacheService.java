package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;

public interface CacheService {
    TokenDto cacheToken(String spotifyId, TokenDto token);

    TokenDto getCachedToken(String spotifyId);

    void evictCachedToken(String spotifyId);
}
