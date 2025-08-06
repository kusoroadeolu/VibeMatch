package com.victor.VibeMatch.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.spotify")
public class SpotifyProviderConfigProperties {
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
    private String apiUri;
}
