package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.cache.CacheService;
import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.jwt.JwtServiceImpl;
import com.victor.VibeMatch.security.CustomUserDetailsService;
import com.victor.VibeMatch.security.Role;
import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserCommandService;
import com.victor.VibeMatch.user.service.UserQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthServiceImpl implements UserAuthService{

    private final UserQueryService userQueryService;
    private final AuthMapper authMapper;
    private final UserCommandService userCommandService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtServiceImpl jwtService;
    private final TokenRefreshService tokenRefreshService;
    private final CacheService cacheService;

    /**
     * Logs a user into the app
     * @param tokenResponse The dto containing the refresh and access token of the user
     * @param userProfile The dto containing the user's spotify profile data
     * @return A response dto containing the user's name, jwt token and refresh token
     * */
    @Transactional
    @Override
    public LoginResponseDto loginUser(SpotifyUserProfile userProfile,
                                      SpotifyTokenResponse tokenResponse){
        String spotifyId = userProfile.getId();

        //Map the spotify token response to the token dto
        TokenDto tokenDto = authMapper.tokenDto(tokenResponse);

        if(spotifyId == null){
            log.error("Spotify ID is null.");
            throw new AuthorizationException("Spotify ID is null");
        }

        saveUserCredentials(spotifyId, tokenResponse, userProfile);

        //Cache the access token after save
        var token = cacheService.cacheToken(spotifyId, tokenDto);


        log.info("Successfully cached token with refresh token: {}", token.refreshToken());

        return authMapper.loginResponseDto(userProfile.getDisplayName(), tokenDto.refreshToken(), assignTokenToUser(spotifyId));
    }

    /**
     * Saves/updates the user's credentials
     * @param spotifyId The spotify id of the user
     * @param tokenResponse The dto containing the refresh and access token of the user
     * @param userProfile The dto containing the user's spotify profile data
     * */
    private void saveUserCredentials(String spotifyId,
                                     SpotifyTokenResponse tokenResponse, SpotifyUserProfile userProfile){

        if(!userQueryService.existsBySpotifyId(spotifyId)){
            userCommandService.saveUser(buildUserCredentials(userProfile, tokenResponse));
        }else{
            var user = userQueryService.findBySpotifyId(spotifyId);
            userCommandService.updateRefreshToken(user, tokenResponse.getRefreshToken());
        }
    }

    /**
     * Loads a user from the DB using their username
     * @param spotifyId The spotify id of the user
     * @return A jwt token assigned to the user
     * */
    private String assignTokenToUser(String spotifyId){
        UserPrincipal userPrincipal =
                 userDetailsService.loadUserBySpotifyId(spotifyId);
        return jwtService.generateToken(userPrincipal);
    }


    /**
     * Builds user credentials to be saved
     * @param spotifyUserProfile The spotify user profile for the user
     * @param tokenResponse The token response dto given by spotify when the user logged in through spotify
     * */
    private User buildUserCredentials(SpotifyUserProfile spotifyUserProfile,
                                      SpotifyTokenResponse tokenResponse){
        return User
                .builder()
                .username(spotifyUserProfile.getDisplayName())
                .spotifyId(spotifyUserProfile.getId())
                .email(spotifyUserProfile.getEmail())
                .refreshToken(tokenResponse.getRefreshToken())
                .country(spotifyUserProfile.getCountry())
                .role(Role.USER)
                .build();
    }


    /**
     * Gets a user's access token
     * @param spotifyId The spotify id of the user
     * @return The access token
     * */
    @Override
    public String getUserAccessToken(String spotifyId){
        validateSpotifyId(spotifyId);

        //Checks if a token exists for a user in the cache, if not refresh their token
        var token = cacheService.getCachedToken(spotifyId);

        LocalDateTime currentDateTime = LocalDateTime.now();

        boolean isExpired = isTokenExpired(token, currentDateTime);
        return !isExpired ? token.accessToken() : tokenRefreshService.refreshUserAccessToken(spotifyId).accessToken();
    }


    //Checks if an access token is null or expired
    private boolean isTokenExpired(TokenDto tokenDto, LocalDateTime currentDateTime){
        if(hasRequiredTokenData(tokenDto)){
            log.error("Access token: {} or Expires In: {} or Created At: {} cannot be null", tokenDto.accessToken(), tokenDto.expiresIn(), tokenDto.createdAt());
            return true;
        }

        var expiresAt = tokenDto.createdAt().plusSeconds(tokenDto.expiresIn());
        return currentDateTime.isAfter(expiresAt);
    }

    //Null checks on required data
    private boolean hasRequiredTokenData(TokenDto tokenDto){
        return tokenDto.accessToken() != null
                        && tokenDto.expiresIn() != null
                        && tokenDto.createdAt() != null;
    }

    /**
     * Logs out a user by clearing their cache
     * @param spotifyId The spotify id of the user
     * */
    @Override
    public void logoutUser(String spotifyId){
        validateSpotifyId(spotifyId);
        cacheService.evictCachedToken(spotifyId);
        log.info("Successfully evicted user info from cache");
    }

    /**
     * Validates Spotify ID
     * @param spotifyId The ID to validate
     * @throws AuthorizationException In the case of an invalid ID
     * */
    public void validateSpotifyId(String spotifyId){
        if(spotifyId == null || !userQueryService.existsBySpotifyId(spotifyId)){
            log.error("Spotify ID is null or does not belong to a user in the DB.");
            throw new AuthorizationException("Spotify ID is null or does not belong to a user in the DB.");
        }
    }


}
