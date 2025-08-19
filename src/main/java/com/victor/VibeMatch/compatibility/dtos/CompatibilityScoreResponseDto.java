package com.victor.VibeMatch.compatibility.dtos;


import java.time.LocalDateTime;
import java.util.List;

public record CompatibilityScoreResponseDto(
        CompatibilityUserDto user,
        CompatibilityUserDto targetUser,
        double discoveryCompatibilityScore,
        double tasteCompatibilityScore,
        List<ArtistCompatibilityDto> sharedArtists,
        List<GenreCompatibilityDto> sharedGenres,
        List<String> whyCompatible,
        LocalDateTime lastCalculated
) {
}
