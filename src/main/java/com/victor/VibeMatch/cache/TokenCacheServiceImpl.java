package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.auth.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCacheServiceImpl implements TokenCacheService {

    private final TokenRefreshService tokenRefreshService;

    @CachePut(cacheNames = "tokenCache", key = "#spotifyId")
    @Override
    public TokenDto cacheToken(String spotifyId, TokenDto token){
        return token;
    }

    @Cacheable(cacheNames = "tokenCache", key = "#spotifyId")
    @Override
    public TokenDto getCachedToken(String spotifyId){
        return tokenRefreshService.refreshUserAccessToken(spotifyId);
    }

    @CacheEvict(key = "#spotifyId", cacheNames = "tokenCache")
    @Override
    public void evictCachedToken(String spotifyId){

    }

}
