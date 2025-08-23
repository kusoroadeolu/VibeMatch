package com.victor.VibeMatch.recommendations.dtos;


import java.util.UUID;

public record RecommendationRequestDto(
        String spotifyUrl,
        String recommendedName,
        UUID recommendedToId,
        String type
) {}

