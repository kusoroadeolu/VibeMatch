package com.victor.VibeMatch.recommendations.dtos;


public record RecommendationRequestDto(
        String spotifyUrl,
        String recommendedName,
        String type
) {}

