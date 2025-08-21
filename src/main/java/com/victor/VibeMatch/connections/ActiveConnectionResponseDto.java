package com.victor.VibeMatch.connections;

import java.time.LocalDateTime;

public record ActiveConnectionResponseDto(
        String requestedBy,
        String sentTo,
        boolean isConnected,
        LocalDateTime connectedSince
) {
}
