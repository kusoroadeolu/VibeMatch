package com.victor.VibeMatch.spotify.client;

import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.dto.SpotifyTopData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
public abstract class SpotifyClientService<T> {

    protected final RestTemplate restTemplate;
    protected final Class<T> responseType;

    /**
     * Builds the URI used to make the request to Spotify's api
     * @param baseUri The base uri
     * @param timeRange The time range of the data to collect
     * @param limit The number of items to get
     * @param scope The scope which will allow spotify to know what to read
     * @return The built uri as a string
     * */
    protected String buildUri(String baseUri, String timeRange,int limit, String scope){
        UriComponents uri = UriComponentsBuilder
                .fromUriString(baseUri)
                .queryParam("time_range", timeRange)
                .queryParam("limit", limit)
                .queryParam("scope", scope)
                .build();
        String requestUri = uri.toUriString();
        log.info("Built request URI: {}", requestUri);
        return requestUri;
    }

    /**
     * Builds the authorization header
     * @param accessToken The token used to request access to the user's account
     * @return The auth header
     */
    protected String buildAuthHeader(String accessToken){
        return "Bearer " + accessToken;
    }

    /**
     * Builds the http entity sent to spotify to retrieve the data
     * @param accessToken The access token
     * @return The http entity used for the request
     * */
    protected HttpEntity<Void> buildRequestEntity(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", buildAuthHeader(accessToken));
        log.info("Successfully set auth header for http entity.");
        return new HttpEntity<>(headers);
    }

    protected void handleSpotifyExceptions(ResponseEntity<SpotifyTopData<T>> entityData) throws SpotifyRateLimitException, AuthorizationException{
        if(entityData.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS){
            log.error("Spotify rate limit threshold exceeded. Please try again later.");
            String retryAfterString = entityData.getHeaders().getFirst("Retry-After");
            long retryAfterSeconds = retryAfterString == null ? 0 : Long.parseLong(retryAfterString);
            throw new SpotifyRateLimitException("Spotify rate limit threshold exceeded. Please try again later.", retryAfterSeconds);
        }else if(entityData.getStatusCode() == HttpStatus.UNAUTHORIZED){
            log.error("Failed to authorize user due to expired access token.");
            throw new AuthorizationException("Failed to authorize user due to expired access token.");
        }
    }

    /**
     * Fetches the needed data from spotify
     * @param accessToken The access token
     * @return A parameterized type of the data
     * */
    abstract ResponseEntity<SpotifyTopData<T>> fetchData(SpotifyDataRequestDto requestDto, String accessToken);
}
