package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.SpotifyProviderConfigProperties;
import com.victor.VibeMatch.auth.SpotifyRegistrationConfigProperties;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import com.victor.VibeMatch.exceptions.AuthorizationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyAuthServiceImpl implements SpotifyAuthService{

    private final SpotifyRegistrationConfigProperties spotifyRegistrationConfigProperties;
    private final SpotifyProviderConfigProperties spotifyProviderConfigProperties;
    private final RestTemplate restTemplate;

    /**
     * Build the auth uri to login in with spotify accounts
     * @return The URI to redirect the user for authorization
     * */
    @Override
    public URI buildAuthUri(){
        String authUrl = UriComponentsBuilder
                .fromUriString(spotifyProviderConfigProperties.getAuthorizationUri())
                .queryParam("client_id", spotifyRegistrationConfigProperties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", spotifyRegistrationConfigProperties.getRedirectUri())
                .queryParam("scope", spotifyRegistrationConfigProperties.getScope())
                .queryParam("state", UUID.randomUUID())
                .build()
                .toUriString();
        log.info("Built authorization URL: {}", authUrl);
        return URI.create(authUrl);
    }

    /**
     * Handles spotify callback after login by exchanging the code for an access token
     * @param code The code given by spotify at login
     * @return A response entity containing the access token
     * */
    @Override
    public ResponseEntity<SpotifyTokenResponse> handleCallback(String code){
        try{
            return createSpotifyToken (code);
        }catch (HttpClientErrorException ex){
            log.info("Failed to exchange code for token. Status: {}. Body: {}", ex.getStatusCode() ,ex.getResponseBodyAsString());
            throw new AuthorizationException(String.format("Failed to exchange code for token. Body: %s", ex.getResponseBodyAsString()));
        }
    }

    /**
     * Creates the request body to send to spotify after callback
     * @param code The authorization code
     * @return A multi value map containing the request body
     */
    public MultiValueMap<String, String> createRequestBody(String code){
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("grant_type", "authorization_code");
        multiValueMap.add("code", code);
        multiValueMap.add("client_id", spotifyRegistrationConfigProperties.getClientId());
        multiValueMap.add("client_secret", spotifyRegistrationConfigProperties.getClientSecret());
        multiValueMap.add("redirect_uri", spotifyRegistrationConfigProperties.getRedirectUri());
        log.info("Successfully created request body for spotify callback.");
        return multiValueMap;
    }

    /**
     * Creates the http entity that will be sent to spotify's post endpoint
     * @param code The authorization code
     * @return A http entity with headers and the request body
     * */
    public HttpEntity<MultiValueMap<String, String>> createHttpEntity(String code){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        log.info("Successfully set content type for request body: {}", headers.getContentType());

        return new HttpEntity<>(createRequestBody(code), headers);
    }

    /**
     * Posts the http entity to spotify and binds it to the response class
     * @param code The authorization code
     * @return A response entity with the spotify token response
     * */
    public ResponseEntity<SpotifyTokenResponse> createSpotifyToken(String code){
        return restTemplate.postForEntity(
                spotifyProviderConfigProperties.getTokenUri(),
                createHttpEntity(code),
                SpotifyTokenResponse.class
        );
    }

    /**
     * Gets the spotify user profile from the spotify api
     * @param authHeader The header passed to spotify api to access the user data
     * @return A dto containing the user data
     * */
    @Override
    public SpotifyUserProfile getSpotifyUser(String authHeader){
        return restTemplate.exchange(
                spotifyProviderConfigProperties.getUserInfoUri(),
                HttpMethod.GET,
                buildSpotifyUserRequest(authHeader),
                SpotifyUserProfile.class
        ).getBody();
    }

    public HttpEntity<Void> buildSpotifyUserRequest(String authHeader){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        log.info("Successfully set authorization header: {}", authHeader);
        return new HttpEntity<>(headers);
    }


    /**
     * Refreshes the access token for a user
     * @param refreshToken The refresh token used to refresh the access token
     * @return A dto containing the refresh token, access token and when it expires
     * */
    @Override
    public SpotifyTokenResponse refreshAccessToken(String refreshToken){
        try{
            log.info("Attempting to refresh access token. Refresh token: {}", refreshToken);
            var response = getTokenResponseEntity(refreshToken).getBody();
            log.info("Successfully refreshed access token. Access token: {}", response.getAccessToken());
            return response;
        }catch (HttpClientErrorException e){
            log.error("An error occurred while trying to refresh an access token. Error message: {}", e.getResponseBodyAsString());
            throw new AuthorizationException(String.format("An error occurred while trying to refresh an access token. Status: %s. Body: %s", e.getStatusCode() ,e.getResponseBodyAsString()));

        }
    }

    /**
     * Creates the response entity containing the access token
     * @param refreshToken The refresh token to refresh the access token
     * @return A response entity of the token response dto
     * */
    public ResponseEntity<SpotifyTokenResponse> getTokenResponseEntity(String refreshToken){
        ResponseEntity<SpotifyTokenResponse> tokenResponseResponseEntity = restTemplate.exchange(
                spotifyProviderConfigProperties.getTokenUri(),
                HttpMethod.POST,
                buildRefreshTokenEntity(refreshToken),
                SpotifyTokenResponse.class
        );
        var tokenResponse = tokenResponseResponseEntity.getBody();

        if (tokenResponse == null){
            log.error("Failed to refresh access token because token entity body is null.");
            throw new AuthorizationException("Failed to refresh access token because token entity body is null.");
        }

        return ResponseEntity.ok(tokenResponse);
    }


    /**
     * Builds the request body to refresh the access token
     * @param refreshToken The refresh token to refresh the access token
     * @return A multi value map containing the request body
     * */
    public MultiValueMap<String, String> createRefreshTokenBody(String refreshToken){
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        return body;
    }

    /**
     * Creates a http entity containing required headers and a refresh token
     * @param refreshToken The refresh token to refresh the access token
     * @return A http entity containing the request body and headers
     * */
    public HttpEntity<MultiValueMap<String, String>> buildRefreshTokenEntity(String refreshToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(spotifyRegistrationConfigProperties.getClientId(), spotifyRegistrationConfigProperties.getClientSecret());
        return new HttpEntity<>(createRefreshTokenBody(refreshToken), headers);
    }


}
