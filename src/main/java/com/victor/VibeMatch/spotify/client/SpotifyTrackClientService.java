package com.victor.VibeMatch.spotify.client;

import com.victor.VibeMatch.exceptions.AuthorizationException;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.spotify.SpotifyDataService;
import com.victor.VibeMatch.spotify.dto.SpotifyDataRequestDto;
import com.victor.VibeMatch.spotify.dto.SpotifyTopData;
import com.victor.VibeMatch.spotify.dto.SpotifyTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Service
@Slf4j
public class SpotifyTrackClientService
        extends SpotifyClientService<SpotifyTrack> implements SpotifyDataService<SpotifyTrack> {
    public SpotifyTrackClientService(RestTemplate restTemplate){
        super(restTemplate, SpotifyTrack.class);
    }

    @Override
    public ArrayList<SpotifyTrack> getTopData(SpotifyDataRequestDto request, String accessToken) throws SpotifyRateLimitException, AuthorizationException {
        var entityData = fetchData(request, accessToken);
        handleSpotifyExceptions(entityData);
        SpotifyTopData<SpotifyTrack> body = entityData.getBody();
        if(body == null){
            log.error("Received null body from spotify call");
            throw new NullPointerException("Received null body from spotify call");
        }

        ArrayList<SpotifyTrack> tracks = body.getItems();
        log.info("Received: {} items from response body.", tracks.size());
        return body.getItems();

    }

    @Override
    protected ResponseEntity<SpotifyTopData<SpotifyTrack>> fetchData(SpotifyDataRequestDto request, String accessToken) {
        return restTemplate.exchange(
                buildUri(request.uri(), request.timeRange(), request.count(), request.scope()),
                HttpMethod.GET,
                buildRequestEntity(accessToken),
                new ParameterizedTypeReference<SpotifyTopData<SpotifyTrack>>() {}
        );
    }
}
