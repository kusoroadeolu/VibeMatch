package com.victor.VibeMatch.usertrack.dtos;

import java.util.List;
import java.util.Set;

public record UserTrackResponseDto(String spotifyId, String name, Set<String> artists, String ownedBy) {
}
