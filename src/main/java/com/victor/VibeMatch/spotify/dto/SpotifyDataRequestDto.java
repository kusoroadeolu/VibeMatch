package com.victor.VibeMatch.spotify.dto;


public record SpotifyDataRequestDto(String uri, String timeRange, int count, String scope) {
}
