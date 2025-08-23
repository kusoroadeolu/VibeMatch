package com.victor.VibeMatch.user;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String email,
        String country,
        String spotifyId,
        String imageUrl
) {
}
