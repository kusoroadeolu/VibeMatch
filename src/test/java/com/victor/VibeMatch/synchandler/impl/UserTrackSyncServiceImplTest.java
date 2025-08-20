package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.auth.service.TokenRefreshService;
import com.victor.VibeMatch.cache.TokenCacheService;
import com.victor.VibeMatch.spotify.DataServiceEnum;
import com.victor.VibeMatch.spotify.SpotifyConfigProperties;
import com.victor.VibeMatch.spotify.SpotifyDataOrchestratorService;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.dto.SpotifyTrack;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.UserTrackUtils;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrackCommandService;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrackQueryService;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrackCommandService;
import com.victor.VibeMatch.usertrack.top.UserTopTrackQueryService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTrackSyncServiceImplTest {

    @Mock
    private TokenCacheService cacheService;
    @Mock
    private TokenRefreshService tokenRefreshService;
    @Mock
    private SpotifyConfigProperties configProperties;
    @Mock
    private UserTrackUtils userTrackUtils;
    @Mock
    private UserRecentTrackCommandService userRecentTrackCommandService;
    @Mock
    private UserTopTrackCommandService userTopTrackCommandService;
    @Mock
    private SpotifyDataOrchestratorService<Object> orchestratorService;
    @Mock
    private UserTopTrackQueryService userTopTrackQueryService;
    @Mock
    private UserRecentTrackQueryService userRecentTrackQueryService;

    @InjectMocks
    private UserTrackSyncServiceImpl<Object> userTrackSyncService;

    private User user;
    private String spotifyId;
    private String accessToken;
    private TokenDto cachedTokenDto;
    private TokenDto refreshedTokenDto;
    private TokenDto invalidTokenDto;
    private List<SpotifyTrack> spotifyTracks;
    private List<UserRecentTrack> userRecentTracks;
    private List<UserTopTrack> userTopTracks;

    @BeforeEach
    void setUp() {
        user = new User();
        spotifyId = "test-spotify-id";
        user.setId(UUID.randomUUID());
        user.setSpotifyId(spotifyId);
        accessToken = "mock-access-token";
        invalidTokenDto = new TokenDto(null, "Bearer", LocalDateTime.now(), 3600, "new-refresh-token", "scope");
        cachedTokenDto = new TokenDto(accessToken, "Bearer", LocalDateTime.now(), 3600, "refresh-token", "scope");
        refreshedTokenDto = new TokenDto("new-access-token", "Bearer", LocalDateTime.now(), 3600, "new-refresh-token", "scope");
        spotifyTracks = List.of(new SpotifyTrack());
        userRecentTracks = List.of(new UserRecentTrack());
        userTopTracks = List.of(new UserTopTrack());

        when(configProperties.getTracksUri()).thenReturn("mock-track-uri");
        when(configProperties.getTrackCount()).thenReturn(10);
        when(configProperties.getScope()).thenReturn("track-scope");
    }

    @Test
    void syncRecentUserTracks_shouldSuccessfullySyncData_whenTokenIsCached() {
        // Arrange
        when(cacheService.getCachedToken(spotifyId)).thenReturn(cachedTokenDto);
        when(configProperties.getShortTime()).thenReturn("short_term");
        when(orchestratorService.fetchSpotifyData(
                eq(DataServiceEnum.TRACKS),
                any(SpotifyDataRequestDto.class),
                eq(accessToken)
        )).thenReturn(new ArrayList<>(spotifyTracks));
        when(userTrackUtils.buildUserRecentTrack(any(SpotifyTrack.class), any(User.class)))
                .thenReturn(new UserRecentTrack());
        when(userRecentTrackQueryService.existsByUser(user)).thenReturn(false);
        when(userRecentTrackCommandService.saveRecentTracks(any())).thenReturn(userRecentTracks);

        // Act
        List<UserRecentTrack> result = userTrackSyncService.syncRecentUserTracks(user);

        // Assert
        assertEquals(userRecentTracks, result);
        verify(cacheService, times(1)).getCachedToken(spotifyId);
        verify(orchestratorService, times(1)).fetchSpotifyData(
                eq(DataServiceEnum.TRACKS),
                any(SpotifyDataRequestDto.class),
                eq(accessToken)
        );
        verify(userRecentTrackCommandService, never()).deleteAllRecentTracksByUser(user);
        verify(userRecentTrackCommandService, times(1)).saveRecentTracks(any());
    }

    @Test
    void syncTopUserTracks_shouldSuccessfullySyncData_whenTokenIsCached() {
        // Arrange
        when(cacheService.getCachedToken(spotifyId)).thenReturn(cachedTokenDto);
        when(configProperties.getMediumTime()).thenReturn("medium_term");
        when(orchestratorService.fetchSpotifyData(
                eq(DataServiceEnum.TRACKS),
                any(SpotifyDataRequestDto.class),
                eq(accessToken)
        )).thenReturn(new ArrayList<>(spotifyTracks));
        when(userTrackUtils.buildUserTopTrack(any(SpotifyTrack.class), any(User.class)))
                .thenReturn(new UserTopTrack());
        when(userTopTrackQueryService.existsByUser(user)).thenReturn(false);
        when(userTopTrackCommandService.saveTopTracks(any())).thenReturn(userTopTracks);

        // Act
        List<UserTopTrack> result = userTrackSyncService.syncTopUserTracks(user);

        // Assert
        assertEquals(userTopTracks, result);
        verify(cacheService, times(1)).getCachedToken(spotifyId);
        verify(orchestratorService, times(1)).fetchSpotifyData(
                eq(DataServiceEnum.TRACKS),
                any(SpotifyDataRequestDto.class),
                eq(accessToken)
        );
        verify(userTopTrackCommandService, never()).deleteAllTopTracksByUser(user);
        verify(userTopTrackCommandService, times(1)).saveTopTracks(any());
    }

    @Test
    void syncRecentUserTracks_shouldRefreshAndSyncData_whenTokenIsNotCached() {
        // Arrange
        when(cacheService.getCachedToken(spotifyId)).thenReturn(invalidTokenDto);
        when(tokenRefreshService.refreshUserAccessToken(spotifyId)).thenReturn(refreshedTokenDto);
        when(configProperties.getShortTime()).thenReturn("short_term");
        when(orchestratorService.fetchSpotifyData(
                eq(DataServiceEnum.TRACKS),
                any(SpotifyDataRequestDto.class),
                eq(refreshedTokenDto.accessToken())
        )).thenReturn(new ArrayList<>(spotifyTracks));
        when(userTrackUtils.buildUserRecentTrack(any(SpotifyTrack.class), any(User.class)))
                .thenReturn(new UserRecentTrack());
        when(userRecentTrackCommandService.saveRecentTracks(any())).thenReturn(userRecentTracks);

        // Act
        List<UserRecentTrack> result = userTrackSyncService.syncRecentUserTracks(user);

        // Assert
        assertEquals(userRecentTracks, result);
        verify(tokenRefreshService, times(1)).refreshUserAccessToken(spotifyId);
        verify(cacheService, times(1)).cacheToken(spotifyId, refreshedTokenDto);
        verify(orchestratorService, times(1)).fetchSpotifyData(
                eq(DataServiceEnum.TRACKS),
                any(SpotifyDataRequestDto.class),
                eq(refreshedTokenDto.accessToken())
        );
    }
}