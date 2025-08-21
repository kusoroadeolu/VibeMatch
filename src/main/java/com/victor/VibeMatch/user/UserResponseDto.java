package com.victor.VibeMatch.user;

public record UserResponseDto(
        String username,
        String email,
        String country,
        String spotifyId
) {
}
