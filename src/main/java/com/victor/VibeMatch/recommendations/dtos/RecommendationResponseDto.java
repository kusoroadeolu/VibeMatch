package com.victor.VibeMatch.recommendations.dtos;

import java.time.LocalDateTime;

public record RecommendationResponseDto(
        String recommenderUsername,
        String spotifyUrl,
        String recommendedName,
        String type,
        LocalDateTime recommendedAt
) {}