package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionQueryService connectionQueryService;
    private final ConnectionCommandService connectionCommandService;
    private final ConnectionMapper connectionMapper;
    private final UserQueryService userQueryService;

    /**
     * Gets all active connections for a user
     * @param userId The ID of the user
     * @return A list of active connections for a user
     * */
    @Override
    public List<ActiveConnectionResponseDto> findAllActiveConnections(UUID userId){
        User user = userQueryService.findByUserId(userId);
        List<Connection> connections = connectionQueryService.findAllActiveConnections(user);
        log.info("Found {} active connections for user: {}", connections.size(), user.getUsername());
        return connections.stream().map(connectionMapper::toActiveResponseDto).toList();
    }

    /**
     * Gets all inactive pending received connections
     * @param userId The ID of the user
     * @return A list of inactive connections for the user
     * */
    @Override
    public List<InactiveConnectionResponseDto> findPendingReceivedConnections(UUID userId){
        User user = userQueryService.findByUserId(userId);
        List<Connection> connections = connectionQueryService.findPendingReceivedConnections(user);
        log.info("Found {} received inactive connections for user: {}", connections.size(), user.getUsername());
        return connections.stream().map(connectionMapper::toInActiveResponseDto).toList();
    }

    /**
     * Gets all inactive sent connection requests
     * @param userId The ID of the user
     * @return A list of inactive connections for the user
     * */
    @Override
    public List<InactiveConnectionResponseDto> findPendingSentConnections(UUID userId){
        User user = userQueryService.findByUserId(userId);
        List<Connection> connections = connectionQueryService.findPendingSentConnections(user);
        log.info("Found {} pending sent connections for user: {}", connections.size(), user.getUsername());
        return connections.stream().map(connectionMapper::toInActiveResponseDto).toList();
    }

    /**
     * Gets all inactive connection requests
     * @param userIdA The ID of user A
     * @param userIdB The ID of user B
     * @return An active connection dto for the user
     * */
    @Override
    public ActiveConnectionResponseDto findActiveConnectionBetweenTwoUsers(UUID userIdA, UUID userIdB){
        User userA = userQueryService.findByUserId(userIdA);
        User userB = userQueryService.findByUserId(userIdB);

        Connection connection = connectionQueryService.findConnection(userA, userB);
        log.info("Found an active connection between user: {} and user: {}", userA.getUsername(), userB.getUsername());
        return connectionMapper.toActiveResponseDto(connection);
    }

    @Override
    public ConnectionWrapperResponseDto requestConnection(UUID requesterId, UUID receiverId){
        User requester = userQueryService.findByUserId(requesterId);
        User receiver = userQueryService.findByUserId(receiverId);

        Connection connection;

        User canonicalUserA = requester;
        User canonicalUserB = receiver;

        if(requesterId.compareTo(receiverId) < 0){
            canonicalUserA = receiver;
            canonicalUserB = requester;
        }


        //Check if you already have a connection with this user
        if(connectionQueryService.activeConnectionExists(canonicalUserA, canonicalUserB)){
            connection = connectionQueryService.findConnection(canonicalUserA, canonicalUserB);
            log.info("You already have an active connection with this user");
            return new ConnectionWrapperResponseDto(connectionMapper.toActiveResponseDto(connection), null);
        }

        Optional<Connection> optionalConnection =
                connectionQueryService.findInactiveConnection(canonicalUserA, canonicalUserB);

        if(optionalConnection.isPresent()){
            connection = optionalConnection.get();
            log.info("{} previously sent a request to {}", connection.getRequester(), connection.getReceiver());
            acceptConnection(connection.getRequester().getId(), connection.getReceiver().getId());
            log.info("Successfully accepted both connection request for both users");
            return new ConnectionWrapperResponseDto(connectionMapper.toActiveResponseDto(connection), null);
        }

        connection = buildConnection(canonicalUserA, canonicalUserB, requester, receiver);

        Connection savedConnection = connectionCommandService.saveConnection(connection);
        log.info("Successfully saved a connection for user");

        return new ConnectionWrapperResponseDto(null, connectionMapper.toInActiveResponseDto(connection));

    }

    @Override
    public void removeConnection(UUID userIdA, UUID userIdB){
        User userA = userQueryService.findByUserId(userIdA);
        User userB = userQueryService.findByUserId(userIdB);
        connectionCommandService.deleteConnection(userA, userB);
    }

    @Override
    public ActiveConnectionResponseDto acceptConnection(UUID requesterId, UUID receiverId){
        User requester = userQueryService.findByUserId(requesterId);
        User receiver = userQueryService.findByUserId(receiverId);
        Connection connection = connectionQueryService.findPendingConnectionBetween(requester, receiver);
        connection.setConnected(true);
        connection.setConnectedSince(LocalDateTime.now());
        Connection savedConnection = connectionCommandService.saveConnection(connection);
        return connectionMapper.toActiveResponseDto(savedConnection);
    }

    public Connection buildConnection(User userA, User userB, User requester, User receiver){
        return Connection
                .builder()
                .userA(userA)
                .userB(userB)
                .requester(requester)
                .receiver(receiver)
                .build();
    }


}
