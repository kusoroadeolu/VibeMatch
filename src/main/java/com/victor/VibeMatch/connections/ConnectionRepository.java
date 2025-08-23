package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {

    @Query("SELECT c FROM Connection c WHERE (c.requester = :user OR c.receiver = :user) AND c.isConnected = true")
    List<Connection> findByRequesterOrReceiverAndIsConnectedTrue(@Param("user") User requester, @Param("user") User receiver);

    Optional<Connection> findByRequesterAndReceiverAndIsConnectedFalse(User requester, User receiver);

    List<Connection> findByRequesterAndIsConnectedFalse(User requester);

    List<Connection> findByReceiverAndIsConnectedFalse(User receiver);

    void deleteByUserAAndUserB(User userA, User userB);

    Optional<Connection> findByUserAAndUserBAndIsConnectedTrue(User userA, User userB);

    boolean existsByRequesterAndReceiverAndIsConnectedFalse(User requester, User receiver);

    boolean existsByUserAAndUserBAndIsConnectedTrue(User userA, User userB);


    Optional<Connection> findByUserAAndUserBAndIsConnectedFalse(User canonicalUserA, User canonicalUserB);
}
