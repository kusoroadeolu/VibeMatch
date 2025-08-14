package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.cache.TokenCacheService;
import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.jwt.JwtServiceImpl;
import com.victor.VibeMatch.security.CustomUserDetailsService;
import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserCommandService;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceImplTest {
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JwtServiceImpl jwtService;
    @Mock
    private SpotifyAuthService spotifyAuthService;
    @Mock
    private TokenCacheService tokenCacheService;
    @Mock
    private TokenRefreshService tokenRefreshService;

    @Mock
    private UserCommandService userCommandService;

    @InjectMocks
    private UserAuthServiceImpl authService;

    private SpotifyTokenResponse tokenResponse;

    private SpotifyUserProfile userProfile;

    private User user;

    private TokenDto tokenDto;

    @BeforeEach
    public void setUp(){
        tokenResponse = new SpotifyTokenResponse(
                "mockAccessToken",
                "mockTokenType",
                3600,
                "mockRefreshToken",
                "mockScope"
        );

        userProfile = new SpotifyUserProfile(
                "mock-name",
                "mock-email",
                "mock-id",
                "mock-country"
        );

         user = User
                .builder()
                .username(userProfile.getDisplayName())
                .email(userProfile.getEmail())
                .spotifyId(userProfile.getId())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();

         tokenDto = new TokenDto(
                "mockAccessToken",
                "mockTokenType",
                LocalDateTime.now(),
                3600,
                "mockRefreshToken",
                "mockScope"
        );
    }


    @Test
    public void loginUser_givenNullSpotifyId_throwAuthorizationException(){
        //Given
        userProfile.setId(null);

        //Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.loginUser(userProfile, tokenResponse);
        });
    }

    @Test
    public void should_login_user(){
        //Given
        UserPrincipal userPrincipal = new UserPrincipal(user);

        String mockJwtToken = "mock-jwt-token";
        String spotifyId = userProfile.getId();

        LoginResponseDto mockedResponseDto = new LoginResponseDto(
                userProfile.getDisplayName(),
                tokenResponse.getRefreshToken(),
                mockJwtToken
        );

        //when
        when(authMapper.tokenDto(tokenResponse)).thenReturn(tokenDto);
        when(userQueryService.existsBySpotifyId(spotifyId)).thenReturn(true);
        when(tokenCacheService.cacheToken(spotifyId, tokenDto)).thenReturn(tokenDto);
        when(userDetailsService.loadUserBySpotifyId(spotifyId)).thenReturn(userPrincipal);
        when(jwtService.generateToken(userPrincipal)).thenReturn(mockJwtToken);
        when(authMapper.loginResponseDto(mockedResponseDto.username(), mockedResponseDto.refreshToken(), mockJwtToken)).thenReturn(mockedResponseDto);

        //Then
        LoginResponseDto responseDto = authService.loginUser(userProfile, tokenResponse);

        //Assert
        assertAll(
                () -> assertNotNull(responseDto),
                () -> assertEquals(mockedResponseDto.username(), responseDto.username()),
                () -> assertEquals(mockedResponseDto.refreshToken(), responseDto.refreshToken()),
                () -> assertEquals(mockedResponseDto.jwtToken(), responseDto.jwtToken())
        );
    }

    @Test
    public void should_throw_exception_on_null_spotify_id(){
        //Assert
        assertThrows(AuthorizationException.class, () -> {
           authService.validateSpotifyId(null);
        });
    }



    @Test
    public void should_get_user_access_token(){
        //Arrange
        String spotifyId = "mock-id";


        //When
        when(tokenCacheService.getCachedToken(spotifyId)).thenReturn(tokenDto);
        when(userQueryService.existsBySpotifyId(spotifyId)).thenReturn(true);
        when(tokenRefreshService.refreshUserAccessToken(spotifyId)).thenReturn(tokenDto);

        //Act
        String accessToken = authService.getUserAccessToken(spotifyId);

        //Assert
        assertAll(
                () -> assertNotNull(accessToken),
                () -> assertEquals(tokenResponse.getAccessToken(), accessToken)
        );

    }

    @Test
    public void onLogout_shouldClearDataFromCache(){
        //Arrange
        String spotifyId = "mockId";

        //When
        when(userQueryService.existsBySpotifyId(spotifyId)).thenReturn(true);

        //Act
        authService.logoutUser(spotifyId);

        //Verify
        verify(tokenCacheService, times(1)).evictCachedToken(spotifyId);


    }

}