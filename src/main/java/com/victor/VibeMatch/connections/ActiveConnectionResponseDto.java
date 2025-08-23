package com.victor.VibeMatch.connections;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActiveConnectionResponseDto(
        String requestedBy,
        String sentTo,
        UUID requestedById,
        UUID sentToId,
        boolean isConnected,
        LocalDateTime connectedSince
) {
}
