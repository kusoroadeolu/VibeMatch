package com.victor.VibeMatch.auth.dtos;

import java.time.LocalDateTime;

public record TokenDto (
        String accessToken,  String tokenType, LocalDateTime createdAt,
        Integer expiresIn, String refreshToken, String scope){
}
