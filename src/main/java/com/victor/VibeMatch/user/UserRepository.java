package com.victor.VibeMatch.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsBySpotifyId(String spotifyId);

    Optional<User> findBySpotifyId(String spotifyId);

    List<User> findByLastSyncedAtBefore(LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findUserBySpotifyIdWithLock(String spotifyId);

    List<User> findByIsPublicTrue();
}
