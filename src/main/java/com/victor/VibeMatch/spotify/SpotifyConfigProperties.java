package com.victor.VibeMatch.spotify;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.spotify")
public class SpotifyConfigProperties {
    private String tracksUri;
    private String artistsUri;
    private String shortTime;
    private String mediumTime;
    private int artistCount;
    private int trackCount;
    private String scope;
}
