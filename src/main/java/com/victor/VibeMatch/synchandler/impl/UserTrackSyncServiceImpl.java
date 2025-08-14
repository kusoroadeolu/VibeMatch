package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.auth.service.TokenRefreshService;
import com.victor.VibeMatch.cache.TokenCacheService;
import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.spotify.SpotifyConfigProperties;
import com.victor.VibeMatch.spotify.SpotifyDataOrchestratorService;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.dto.SpotifyTrack;
import com.victor.VibeMatch.synchandler.services.UserTrackSyncService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.UserTrackUtils;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrackCommandService;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrackCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.victor.VibeMatch.spotify.DataServiceEnum.TRACKS;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserTrackSyncServiceImpl<T> implements UserTrackSyncService {
    private final TokenCacheService cacheService;
    private final TokenRefreshService tokenRefreshService;
    private final SpotifyConfigProperties configProperties;
    private final UserTrackUtils userTrackUtils;
    private final UserRecentTrackCommandService userRecentTrackCommandService;
    private final UserTopTrackCommandService userTopTrackCommandService;
    private final SpotifyDataOrchestratorService<T> orchestratorService;

    /**
     * Sync User Recent Track Data
     * @param spotifyId The spotifyId of the user
     * */
    @Override
    public List<UserRecentTrack> syncRecentUserTracks(User user, String spotifyId){
        log.info("Initiating sync for {} recent tracks.", user.getUsername());


        List<SpotifyTrack> recentTracks = fetchTracks(spotifyId, buildRecentTrackDto());
        log.info("Successfully fetched: {} recent tracks for user: {}", recentTracks.size(), user.getUsername());

        List<UserRecentTrack> userRecentTracks = recentTracks
                .stream()
                .map(recentTrack -> userTrackUtils.buildUserRecentTrack(recentTrack, user))
                .toList();
        return userRecentTrackCommandService.saveRecentTracks(userRecentTracks);
    }

    /**
     * Sync User Top Track Data
     * @param spotifyId The spotifyId of the user
     * */
    @Override
    public List<UserTopTrack> syncTopUserTracks(User user, String spotifyId){
        log.info("Initiating sync for {} top tracks.", user.getUsername());

        List<SpotifyTrack> recentTracks = fetchTracks(spotifyId, buildTopTrackDto());
        log.info("Successfully fetched: {} top tracks for user: {}", recentTracks.size(), user.getUsername());

        List<UserTopTrack> userTopTracks = recentTracks
                .stream()
                .map(recentTrack -> userTrackUtils.buildUserTopTrack(recentTrack, user))
                .toList();

        userTopTracks.forEach(userTopTrack -> System.out.println("TRACK NAME: " + userTopTrack.getName() + "\n"));

        return userTopTrackCommandService.saveTopTracks(userTopTracks);
    }

    private SpotifyDataRequestDto buildTopTrackDto(){
        return new SpotifyDataRequestDto(
            configProperties.getTracksUri(), configProperties.getMediumTime(), configProperties .getTrackCount(), configProperties.getScope()
        );
    }

    private SpotifyDataRequestDto buildRecentTrackDto(){
        return new SpotifyDataRequestDto(
                configProperties.getTracksUri(), configProperties.getShortTime(), configProperties .getTrackCount(), configProperties.getScope()
        );
    }

    @Retryable(
            retryFor = AuthorizationException.class,
            backoff = @Backoff(delay = 2000L, multiplier = 4)
    )
    private List<SpotifyTrack> fetchTracks(String spotifyId, SpotifyDataRequestDto requestDto){
        String accessToken = handleAccessToken(spotifyId);

        ArrayList<SpotifyTrack> spotifyTracks = new ArrayList<>();
        try{
            spotifyTracks = orchestratorService.fetchSpotifyData(TRACKS, requestDto, accessToken);
        }catch (AuthorizationException e){
            log.error("Access token expired, will retry after refresh");
            cacheService.evictCachedToken(spotifyId);
            throw e;
        }
        return spotifyTracks;
    }

    private String handleAccessToken(String spotifyId){
        String accessToken = cacheService.getCachedToken(spotifyId).accessToken();
        if(accessToken == null){
            var tokenDto = tokenRefreshService.refreshUserAccessToken(spotifyId);
            cacheService.cacheToken(spotifyId, tokenDto);
            accessToken = tokenDto.accessToken();
        }
        return accessToken;
    }

}
