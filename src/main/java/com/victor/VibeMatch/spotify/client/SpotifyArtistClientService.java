package com.victor.VibeMatch.spotify.client;

import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.spotify.SpotifyDataService;
import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.spotify.dto.SpotifyTopData;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;

@Service
@Slf4j
public class SpotifyArtistClientService extends SpotifyClientService<SpotifyArtist>
        implements SpotifyDataService<SpotifyArtist> {


    public SpotifyArtistClientService(RestTemplate restTemplate){
        super(restTemplate, SpotifyArtist.class);
    }

    @Override
    public ArrayList<SpotifyArtist> getTopData(SpotifyDataRequestDto request, String accessToken) throws SpotifyRateLimitException, AuthorizationException{
        var entityData = fetchData(request, accessToken);
        handleSpotifyExceptions(entityData);
        SpotifyTopData<SpotifyArtist> body = entityData.getBody();

        if(body == null){
            log.error("Received null body from spotify call");
            throw new NullPointerException("Received null body from spotify call");
        }

        ArrayList<SpotifyArtist> artists = body.getItems();
        log.info("Received: {} items from response body.", artists.size());
        return artists;
    }

    @Override
    protected ResponseEntity<SpotifyTopData<SpotifyArtist>> fetchData(SpotifyDataRequestDto request, String accessToken) {
        log.info("Request URI: {}", request.uri());
        return restTemplate.exchange(
                buildUri(request.uri(), request.timeRange(), request.count(), request.scope()),
                HttpMethod.GET,
                buildRequestEntity(accessToken),
                new ParameterizedTypeReference<SpotifyTopData<SpotifyArtist>>() {}
        );
    }
}
