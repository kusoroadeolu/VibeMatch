package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import org.springframework.http.ResponseEntity;

import java.net.URI;

public interface SpotifyAuthService {
    URI buildAuthUri();
    ResponseEntity<SpotifyTokenResponse> handleCallback(String code);
    SpotifyUserProfile getSpotifyUser(String authHeader);
    SpotifyTokenResponse refreshAccessToken(String refreshToken);
}
