package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.auth.service.TokenRefreshService;
import com.victor.VibeMatch.cache.TokenCacheService;
import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.spotify.SpotifyConfigProperties;
import com.victor.VibeMatch.spotify.SpotifyDataOrchestratorService;
import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.synchandler.services.UserArtistSyncService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.userartist.UserArtistCommandService;
import com.victor.VibeMatch.userartist.UserArtistCommandServiceImpl;
import com.victor.VibeMatch.userartist.mapper.UserArtistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.victor.VibeMatch.spotify.DataServiceEnum.ARTISTS;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserArtistSyncServiceImpl<T> implements UserArtistSyncService {

    private final TokenCacheService tokenCacheService;
    private final TokenRefreshService tokenRefreshService;
    private final SpotifyConfigProperties configProperties;
    private final UserArtistMapper userArtistMapper;
    private final UserArtistCommandService userArtistCommandService;
    private final SpotifyDataOrchestratorService<T> orchestratorService;

    /**
     * Sync User Artist Data
     * @param spotifyId The spotify ID of the user
     * */
    @Override
    public List<UserArtist> syncUserArtist(User user, String spotifyId){
        log.info("Initiating sync for {} top artists.", user.getUsername());

        List<SpotifyArtist> spotifyArtists = fetchUserTopArtist(spotifyId);

        List<UserArtist> userArtists = spotifyArtists
                .stream()
                .map(artist -> userArtistMapper.buildUserArtist(user, artist))
                .toList();

        return userArtistCommandService.saveUserArtists(userArtists);
    }

    @Retryable(
            retryFor = AuthorizationException.class,
            backoff = @Backoff(delay = 2000L, multiplier = 4)
    )
    private List<SpotifyArtist> fetchUserTopArtist(String spotifyId){
        String accessToken = handleAccessToken(spotifyId);
        var requestDto = new SpotifyDataRequestDto(configProperties.getArtistsUri(),
                configProperties.getMediumTime(),
                configProperties.getArtistCount(),
                configProperties.getScope());
        log.info("Artist URI: {}", configProperties.getArtistsUri());

        ArrayList<SpotifyArtist> spotifyArtists = new ArrayList<>();
        try{
            spotifyArtists = orchestratorService.fetchSpotifyData(ARTISTS, requestDto, accessToken);
        }catch (AuthorizationException e){
            log.error("Access token expired, will retry after refresh");
            tokenCacheService.evictCachedToken(spotifyId);
            throw e;
        }
        return spotifyArtists;
    }

    private String handleAccessToken(String spotifyId){
        String accessToken = tokenCacheService.getCachedToken(spotifyId).accessToken();
        if(accessToken == null){
            var tokenDto = tokenRefreshService.refreshUserAccessToken(spotifyId);
            tokenCacheService.cacheToken(spotifyId, tokenDto);
            accessToken = tokenDto.accessToken();
        }
        return accessToken;
    }

}
