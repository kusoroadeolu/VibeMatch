package com.victor.VibeMatch.connections;


import java.time.LocalDateTime;
import java.util.UUID;

public record InactiveConnectionResponseDto(
        String requestedBy,
        String sentTo,
        UUID requestedById,
        UUID sentToId,
        boolean isConnected,
        LocalDateTime sentAt
) {
}
