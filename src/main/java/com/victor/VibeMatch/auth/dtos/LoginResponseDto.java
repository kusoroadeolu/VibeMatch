package com.victor.VibeMatch.auth.dtos;

public record LoginResponseDto(
        String username,
        String refreshToken,
        String jwtToken
) {
}
