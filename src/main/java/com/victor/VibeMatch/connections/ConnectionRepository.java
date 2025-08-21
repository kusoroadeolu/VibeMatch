package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {

    List<Connection> findByRequesterOrReceiverAndIsConnectedTrue(User requester, User receiver);

    Optional<Connection> findByRequesterAndReceiverAndIsConnectedFalse(User requester, User receiver);

    List<Connection> findByRequesterAndIsConnectedFalse(User requester);

    List<Connection> findByReceiverAndIsConnectedFalse(User receiver);

    void deleteByUserAAndUserB(User userA, User userB);

    Optional<Connection> findByUserAAndUserBAndIsConnectedTrue(User userA, User userB);

    boolean existsByRequesterAndReceiverAndIsConnectedFalse(User requester, User receiver);

    boolean existsByUserAAndUserBAndIsConnectedTrue(User userA, User userB);


    Optional<Connection> findByUserAAndUserBAndIsConnectedFalse(User canonicalUserA, User canonicalUserB);
}
