package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRefreshServiceImplTest {

    @Mock
    private SpotifyAuthService spotifyAuthService;

    @Mock
    private  UserQueryService userQueryService;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private TokenRefreshServiceImpl tokenRefreshService;

    @Test
    public void should_refresh_user_access_token(){
        //Arrange
        String refreshToken = "mock-token";
        String spotifyId = "mock-id";
        var tokenResponse = new SpotifyTokenResponse(
                "mockAccessToken",
                "mockTokenType",
                3600,
                "mockRefreshToken",
                "mockScope"
        );

        var tokenDto = new TokenDto(
                "mockAccessToken",
                "mockTokenType",
                LocalDateTime.now(),
                3600,
                "mockRefreshToken",
                "mockScope"
        );
        var user = User.builder().refreshToken(refreshToken).build();


        //When
        when(userQueryService.findBySpotifyId(spotifyId)).thenReturn(user);
        when(spotifyAuthService.refreshAccessToken(refreshToken)).thenReturn(tokenResponse);
        when(authMapper.tokenDto(tokenResponse)).thenReturn(tokenDto);

        //Act
        TokenDto token = tokenRefreshService.refreshUserAccessToken(spotifyId);

        //Assert
        assertAll(
                () -> assertNotNull(token, "The token created should not be null")
        );
        verify(userQueryService, times(1)).findBySpotifyId(spotifyId);
        verify(spotifyAuthService, times(1)).refreshAccessToken(refreshToken);
        verify(authMapper, times(1)).tokenDto(tokenResponse);


    }

}