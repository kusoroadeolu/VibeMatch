package com.victor.VibeMatch.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.spotify")
@Getter
@Setter
public class SpotifyRegistrationConfigProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
}
