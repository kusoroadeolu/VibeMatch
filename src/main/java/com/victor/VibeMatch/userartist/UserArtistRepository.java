package com.victor.VibeMatch.userartist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserArtistRepository extends JpaRepository<UserArtist, UUID> {
}
