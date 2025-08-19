package com.victor.VibeMatch.compatibility.dtos;

public record GenreCompatibilityDto(
        String genreName,
        double yourPercentage,
        double theirPercentage
) {
}
