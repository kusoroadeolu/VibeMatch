package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;

import java.util.List;
import java.util.Optional;

public interface ConnectionQueryService {
    List<Connection> findAllActiveConnections(User user);

    List<Connection> findPendingSentConnections(User requester);

    List<Connection> findPendingReceivedConnections(User requester);

    Connection findConnection(User userA, User userB);

    Optional<Connection> findInactiveConnection(User userA, User userB);

    Connection findPendingConnectionBetween(User requester, User receiver);

    boolean pendingRequestExists(User requester, User receiver);

    boolean activeConnectionExists(User userA, User userB);
}
