package com.victor.VibeMatch.recommendations.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecommendationResponseDto(
        String recommenderUsername,
        String recommendedItemName,
        String spotifyUrl,
        String type,
        LocalDateTime recommendedAt
) {}