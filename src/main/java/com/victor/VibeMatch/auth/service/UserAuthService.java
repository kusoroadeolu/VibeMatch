package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import jakarta.transaction.Transactional;

public interface UserAuthService {

    LoginResponseDto loginUser(SpotifyUserProfile userProfile,
                               SpotifyTokenResponse tokenResponse);

    String getUserAccessToken(String spotifyId);

    void logoutUser(String username);
}
