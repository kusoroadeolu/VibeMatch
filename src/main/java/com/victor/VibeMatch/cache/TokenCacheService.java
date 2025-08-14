package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.auth.dtos.TokenDto;

public interface TokenCacheService {
    TokenDto cacheToken(String spotifyId, TokenDto token);

    TokenDto getCachedToken(String spotifyId);

    void evictCachedToken(String spotifyId);
}
