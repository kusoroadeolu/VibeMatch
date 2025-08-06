package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserResponseDto;

public interface UserQueryService {
    User findByUsername(String username);

    UserResponseDto getUserData(String spotifyId);

    User findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);
}
