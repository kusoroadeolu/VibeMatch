package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserResponseDto;

import java.util.UUID;

public interface UserQueryService {
    User findByUsername(String username);

    User findByUserId(UUID userId);

    UserResponseDto getUserData(String spotifyId);

    User findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);
}
