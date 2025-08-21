package com.victor.VibeMatch.connections;


import java.time.LocalDateTime;

public record InactiveConnectionResponseDto(
        String requestedBy,
        String sentTo,
        boolean isConnected,
        LocalDateTime sentAt
) {
}
