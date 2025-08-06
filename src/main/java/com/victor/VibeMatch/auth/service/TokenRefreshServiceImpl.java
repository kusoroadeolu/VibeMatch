package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshServiceImpl implements TokenRefreshService{

    private final SpotifyAuthService spotifyAuthService;
    private final UserQueryService userQueryService;
    private final AuthMapper authMapper;

    /**
     * Refreshes a user's access token
     * @param spotifyId The spotify id of the user
     * @return A response dto containing the refresh and access token
     * */
    @Override
    public TokenDto refreshUserAccessToken(String spotifyId){
        User user = userQueryService.findBySpotifyId(spotifyId);
        SpotifyTokenResponse tokenResponse = spotifyAuthService.refreshAccessToken(user.getRefreshToken());
        return authMapper.tokenDto(tokenResponse);
    }
}
