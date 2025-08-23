package com.victor.VibeMatch.tasteprofile.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TasteProfileResponseDto(
        String userId,
        String username,
        boolean isPublic,
        List<GenreDto> topGenres,
        List<ArtistDto> topArtists,
        double mainstreamScore,
        String howYouListen,
        LocalDateTime lastUpdated
) {
}
