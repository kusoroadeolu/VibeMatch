package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserQueryService {
    User findByUsername(String username);

    User findByUserId(UUID userId);

    UserResponseDto getUserData(String spotifyId);

    User findBySpotifyId(String spotifyId);

    Optional<User> findOptionalUserBySpotifyId(String spotifyId);

    List<User> findAllUsers();

    boolean existsBySpotifyId(String spotifyId);

    List<User> findAllPublicUsers();

    List<User> findByLastSyncedAtBefore(LocalDateTime then);
}
