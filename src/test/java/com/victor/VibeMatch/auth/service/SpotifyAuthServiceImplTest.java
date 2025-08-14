package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.SpotifyProviderConfigProperties;
import com.victor.VibeMatch.auth.SpotifyRegistrationConfigProperties;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import com.victor.VibeMatch.exceptions.AuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpotifyAuthServiceImplTest {

    @Mock
    private SpotifyProviderConfigProperties providerConfigProperties;

    @Mock
    private SpotifyRegistrationConfigProperties registrationConfigProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SpotifyAuthServiceImpl spotifyAuthService;

    // A shared variable can still be set up here
    private SpotifyTokenResponse tokenResponse;

    @BeforeEach
    public void setUp(){
        tokenResponse = new SpotifyTokenResponse(
                "mockAccessToken",
                "mockTokenType",
                3600,
                "mockRefreshToken",
                "mockScope"
        );
    }

    @Test
    public void should_build_auth_url(){
        when(registrationConfigProperties.getClientId()).thenReturn("mock-client-id");
        when(registrationConfigProperties.getRedirectUri()).thenReturn("http://127.0.0.1/auth/callback");
        when(registrationConfigProperties.getScope()).thenReturn("user-mock-scope");
        when(providerConfigProperties.getAuthorizationUri()).thenReturn("https://accounts.spotify.com/authorize");

        //Act
        URI authUri = spotifyAuthService.buildAuthUri();

        var uriComponents = UriComponentsBuilder.fromUri(authUri).build();
        var state = uriComponents.getQueryParams().getFirst("state");

        //Assert
        assertAll(
                () -> assertNotNull(authUri),
                () -> assertEquals("https://accounts.spotify.com/authorize", authUri.getScheme() + "://" + authUri.getAuthority() + authUri.getPath()),
                () -> assertEquals("mock-client-id", uriComponents.getQueryParams().getFirst("client_id")),
                () -> assertEquals("user-mock-scope", uriComponents.getQueryParams().getFirst("scope")),
                () -> assertEquals("http://127.0.0.1/auth/callback", uriComponents.getQueryParams().getFirst("redirect_uri")),
                () -> assertEquals("code", uriComponents.getQueryParams().getFirst("response_type")),
                () -> assertNotNull(state),
                () -> assertDoesNotThrow(() -> UUID.fromString(state))
        );
    }

    @Test
    public void should_handle_callback(){
        String code = "mock-code";
        when(providerConfigProperties.getTokenUri()).thenReturn("https://accounts.spotify.com/api/token");
        when(registrationConfigProperties.getClientId()).thenReturn("mock-client-id");
        when(registrationConfigProperties.getClientSecret()).thenReturn("mock-client-secret");
        when(registrationConfigProperties.getRedirectUri()).thenReturn("http://127.0.0.1/auth/callback");

        when(restTemplate.postForEntity(
                any(String.class),
                any(HttpEntity.class),
                eq(SpotifyTokenResponse.class)
        )).thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        //Act
        ResponseEntity<SpotifyTokenResponse> tokenResponseEntity = spotifyAuthService.handleCallback(code);

        //Assert
        assertAll(
                () -> assertNotNull(tokenResponseEntity),
                () -> assertEquals(HttpStatus.OK, tokenResponseEntity.getStatusCode()),
                () -> assertNotNull(tokenResponseEntity.getBody()),
                () -> assertEquals(tokenResponse, tokenResponseEntity.getBody())
        );
    }

    @Test
    public void handleCallback_givenInvalidCode_shouldThrowAuthorizationException(){
        String invalidCode = "bad-auth-code";

        when(providerConfigProperties.getTokenUri()).thenReturn("https://accounts.spotify.com/api/token");
        when(registrationConfigProperties.getClientId()).thenReturn("mock-client-id");
        when(registrationConfigProperties.getClientSecret()).thenReturn("mock-client-secret");
        when(registrationConfigProperties.getRedirectUri()).thenReturn("http://127.0.0.1/auth/callback");
        when(restTemplate.postForEntity(
                any(String.class),
                any(HttpEntity.class),
                eq(SpotifyTokenResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Mocked bad request"));

        assertThrows(AuthorizationException.class, () -> {
            spotifyAuthService.handleCallback(invalidCode);
        });
    }

    @Test
    public void should_get_spotify_user(){
        SpotifyUserProfile mockProfile = new SpotifyUserProfile(
                "mock-name",
                "mock-email",
                "mock-id",
                "mock-country"
        );
        ResponseEntity<SpotifyUserProfile> mockProfileEntity = new ResponseEntity<>(mockProfile, HttpStatus.OK);
        String authToken = "Bearer mock-token";

        //When
        when(providerConfigProperties.getUserInfoUri())
                .thenReturn("mock-uri");
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(SpotifyUserProfile.class)
        )).thenReturn(mockProfileEntity);

        //Then
        SpotifyUserProfile userProfile = spotifyAuthService.getSpotifyUser(authToken);

        assertAll(
                () -> assertNotNull(userProfile),
                () -> assertEquals(mockProfile.getDisplayName(), userProfile.getDisplayName()),
                () -> assertEquals(mockProfile.getId(), userProfile.getId()),
                () -> assertEquals(mockProfile, userProfile)
        );
    }

    @Test
    public void should_refresh_access_token(){
        //Arrange
        String refreshToken = "mock-refresh-token";

       //When
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(SpotifyTokenResponse.class)
        )).thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(providerConfigProperties.getApiUri()).thenReturn("mock-uri");
        when(registrationConfigProperties.getClientId()).thenReturn("mock-client-id");
        when(registrationConfigProperties.getClientSecret()).thenReturn("mock-client-secret");

        SpotifyTokenResponse mockTokenResponse = spotifyAuthService.refreshAccessToken(refreshToken);

        //Then
        assertAll(
                () -> assertNotNull(mockTokenResponse),
                () -> assertEquals(tokenResponse, mockTokenResponse),
                () -> assertEquals(tokenResponse.getAccessToken(), mockTokenResponse.getAccessToken())
        );

    }

    @Test
    public void refreshAccessToken_givenNullResponseToken_throwAuthorizationException(){
        //Arrange
        String refreshToken = "invalid-refresh-token";

        //When
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(SpotifyTokenResponse.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        when(providerConfigProperties.getApiUri()).thenReturn("mock-uri");
        when(registrationConfigProperties.getClientId()).thenReturn("mock-client-id");
        when(registrationConfigProperties.getClientSecret()).thenReturn("mock-client-secret");

        //Assert
        assertThrows(AuthorizationException.class, () -> {
            spotifyAuthService.refreshAccessToken(refreshToken);
        });

    }

}