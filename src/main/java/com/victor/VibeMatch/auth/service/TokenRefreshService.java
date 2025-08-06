package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;

public interface TokenRefreshService {
    TokenDto refreshUserAccessToken(String spotifyId);
}
