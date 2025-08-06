package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.auth.service.TokenRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
class CacheServiceImplTest {

    @Autowired
    private CacheServiceImpl cacheService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private TokenRefreshService tokenRefreshService;

    private SpotifyTokenResponse spotifyTokenResponse;

    private Cache cache;

    private TokenDto tokenDto;

    private LocalDateTime createdAt = LocalDateTime.now();

    @BeforeEach
    public void setUp(){
        spotifyTokenResponse = new SpotifyTokenResponse(
              "mock-access-token",
              "mock-token-type",
              10,
              "mock-refresh-token",
              "scope"
        );

        tokenDto = new TokenDto(
                "mockAccessToken",
                "mockTokenType",
                createdAt,
                3600,
                "mockRefreshToken",
                "mockScope"
        );

        cache = cacheManager.getCache("tokenCache");

        if(cache != null){
            cache.clear();
        }
    }

    @Test
    public void cacheToken_shouldCacheToken_whenGivenSpotifyId(){
        //Arrange
        String spotifyId = "mock-id-put";

        //Act
        cacheService.cacheToken(spotifyId, tokenDto);

        //Assert
        Cache.ValueWrapper cachedValue = cache.get(spotifyId);
        assertNotNull(cachedValue, "Cache should contain the token after the method call");
        assertEquals(tokenDto, cachedValue.get(), "The cached token should be the same as the one put in");
    }

    @Test
    public void getCachedToken_shouldReturnCachedToken_onSecondCall(){
        //Arrange
        String spotifyId = "mock-id-get";

        //When
        when(tokenRefreshService.refreshUserAccessToken(spotifyId)).thenReturn(tokenDto);

        //Act
        TokenDto tokenResponse1 = cacheService.getCachedToken(spotifyId);

        //Assert
        assertNotNull(cache.get(spotifyId), "Cache should contain the token after the first call");

        //Act
        TokenDto tokenResponse2 = cacheService.getCachedToken(spotifyId);

        //Assert
        assertNotNull(tokenResponse2);
        assertEquals(tokenResponse1, tokenResponse2, "The second call should return the same instance from the cache");
        verify(tokenRefreshService, times(1)).refreshUserAccessToken(spotifyId);
}

    @Test
    public void evictCachedToken_shouldEvictCachedToken_onMethodCall(){
        //Arrange
        String spotifyId = "mock-id-evict";

        //Act
        cacheService.cacheToken(spotifyId, tokenDto);

        //Assert
        assertNotNull(cache.get(spotifyId), "Cache should contain the token before eviction");

        //Act
        cacheService.evictCachedToken(spotifyId);

        //Assert
        assertNull(cache.get(spotifyId), "Cache should not contain the token after eviction");

    }



}