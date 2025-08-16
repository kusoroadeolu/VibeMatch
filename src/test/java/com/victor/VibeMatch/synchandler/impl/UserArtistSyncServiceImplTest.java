package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.auth.service.TokenRefreshService;
import com.victor.VibeMatch.cache.TokenCacheService;
import com.victor.VibeMatch.spotify.DataServiceEnum;
import com.victor.VibeMatch.spotify.SpotifyConfigProperties;
import com.victor.VibeMatch.spotify.SpotifyDataOrchestratorService;
import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.userartist.UserArtistCommandServiceImpl;
import com.victor.VibeMatch.userartist.mapper.UserArtistMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserArtistSyncServiceImplTest {

    @Mock
    private TokenCacheService tokenCacheService;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @Mock
    private SpotifyConfigProperties configProperties;

    @Mock
    private UserArtistMapper userArtistMapper;

    @Mock
    private UserArtistCommandServiceImpl userArtistCommandService;

    @Mock
    private SpotifyDataOrchestratorService<Object> orchestratorService;

    @InjectMocks
    private UserArtistSyncServiceImpl<Object> userArtistSyncService;

    private User user;
    private String spotifyId;
    private String accessToken;
    private TokenDto cachedTokenDto;
    private TokenDto invalidTokenDto;
    private TokenDto refreshedTokenDto;
    private List<SpotifyArtist> spotifyArtists;
    private List<UserArtist> userArtists;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        spotifyId = "test-spotify-id";
        accessToken = "mock-access-token";

        // Use the new TokenDto structure
        invalidTokenDto = new TokenDto(null, "Bearer", LocalDateTime.now(), 3600, "new-refresh-token", "scope");
        cachedTokenDto = new TokenDto(accessToken, "Bearer", LocalDateTime.now(), 3600, "refresh-token", "scope");
        refreshedTokenDto = new TokenDto("new-access-token", "Bearer", LocalDateTime.now(), 3600, "new-refresh-token", "scope");

        spotifyArtists = List.of(new SpotifyArtist());
        userArtists = List.of(new UserArtist());
    }

    @Test
    void syncUserArtist_shouldSuccessfullySyncData_whenTokenIsCached() {
        // Arrange
        when(tokenCacheService.getCachedToken(spotifyId)).thenReturn(cachedTokenDto);

        when(configProperties.getArtistsUri()).thenReturn("mock-uri");
        when(configProperties.getMediumTime()).thenReturn("medium");
        when(configProperties.getArtistCount()).thenReturn(5);
        when(configProperties.getScope()).thenReturn("scope");

        when(orchestratorService.fetchSpotifyData(
                eq(DataServiceEnum.ARTISTS),
                any(SpotifyDataRequestDto.class),
                eq(accessToken)
        )).thenReturn(new ArrayList<>(spotifyArtists));

        when(userArtistMapper.buildUserArtist(any(User.class), any(SpotifyArtist.class)))
                .thenReturn(new UserArtist());
        when(userArtistCommandService.saveUserArtists(any())).thenReturn(userArtists);

        // Act
        List<UserArtist> result = userArtistSyncService.syncUserArtist(user, spotifyId);

        // Assert
        assertEquals(userArtists, result);

        verify(tokenCacheService, times(1)).getCachedToken(spotifyId);
        verify(tokenRefreshService, never()).refreshUserAccessToken(any());

        verify(orchestratorService, times(1)).fetchSpotifyData(
                eq(DataServiceEnum.ARTISTS),
                any(SpotifyDataRequestDto.class),
                eq(accessToken)
        );
        verify(userArtistCommandService, times(1)).saveUserArtists(any());
    }

    @Test
    void syncUserArtist_shouldRefreshAndSyncData_whenTokenIsNotCached() {
        // Arrange

        when(tokenCacheService.getCachedToken(spotifyId)).thenReturn(invalidTokenDto);
        when(tokenRefreshService.refreshUserAccessToken(spotifyId)).thenReturn(refreshedTokenDto);

        when(configProperties.getArtistsUri()).thenReturn("mock-uri");
        when(configProperties.getMediumTime()).thenReturn("medium");
        when(configProperties.getArtistCount()).thenReturn(5);
        when(configProperties.getScope()).thenReturn("scope");

        when(orchestratorService.fetchSpotifyData(
                eq(DataServiceEnum.ARTISTS),
                any(SpotifyDataRequestDto.class),
                eq(refreshedTokenDto.accessToken())
        )).thenReturn(new ArrayList<>(spotifyArtists));

        when(userArtistMapper.buildUserArtist(any(User.class), any(SpotifyArtist.class)))
                .thenReturn(new UserArtist());
        when(userArtistCommandService.saveUserArtists(any())).thenReturn(userArtists);

        // Act
        List<UserArtist> result = userArtistSyncService.syncUserArtist(user, spotifyId);

        // Assert
        assertEquals(userArtists, result);

        verify(tokenCacheService, times(1)).getCachedToken(spotifyId);
        verify(tokenRefreshService, times(1)).refreshUserAccessToken(spotifyId);
        verify(tokenCacheService, times(1)).cacheToken(spotifyId, refreshedTokenDto);

        verify(orchestratorService, times(1)).fetchSpotifyData(
                eq(DataServiceEnum.ARTISTS),
                any(SpotifyDataRequestDto.class),
                eq(refreshedTokenDto.accessToken())
        );
        verify(userArtistCommandService, times(1)).saveUserArtists(any());
    }
}