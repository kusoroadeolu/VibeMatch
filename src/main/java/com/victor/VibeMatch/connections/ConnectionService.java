package com.victor.VibeMatch.connections;

import java.util.List;
import java.util.UUID;

public interface ConnectionService {
    List<ActiveConnectionResponseDto> findAllActiveConnections(UUID userId);

    List<InactiveConnectionResponseDto> findPendingReceivedConnections(UUID userId);

    List<InactiveConnectionResponseDto> findPendingSentConnections(UUID userId);

    ActiveConnectionResponseDto findActiveConnectionBetweenTwoUsers(UUID userIdA, UUID userIdB);

    ConnectionWrapperResponseDto requestConnection(UUID requesterId, UUID receiverId);

    void removeConnection(UUID userIdA, UUID userIdB);

    ActiveConnectionResponseDto acceptConnection(UUID requesterId, UUID receiverId);
}
