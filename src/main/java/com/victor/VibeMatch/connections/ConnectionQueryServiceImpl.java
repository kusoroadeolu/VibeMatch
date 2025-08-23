package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.exceptions.NoSuchConnectionException;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionQueryServiceImpl implements ConnectionQueryService {

    private final ConnectionRepository connectionRepository;

    @Override
    public List<Connection> findAllActiveConnections(User user){
        return connectionRepository
                .findByRequesterOrReceiverAndIsConnectedTrue(user, user);
    }


    @Override
    public List<Connection> findPendingSentConnections(User requester){
        return connectionRepository.findByRequesterAndIsConnectedFalse(requester);
    }

    @Override
    public List<Connection> findPendingReceivedConnections(User receiver){
        return connectionRepository.findByReceiverAndIsConnectedFalse(receiver);
    }

    @Override
    public Connection findConnection(User userA, User userB){
        User canonicalUserA = userA;
        User canonicalUserB = userB;

        if(userA.getId().compareTo(userB.getId()) < 0){
            canonicalUserA = userB;
            canonicalUserB = userA;
        }

        return connectionRepository.findByUserAAndUserBAndIsConnectedTrue(canonicalUserA, canonicalUserB)
                .orElseThrow(() -> new NoSuchConnectionException("Failed to find an active connection between both users"));
    }

    @Override
    public Optional<Connection> findInactiveConnection(User userA, User userB){
        return connectionRepository.findByUserAAndUserBAndIsConnectedFalse(userA, userB);
    }

    @Override
    public Connection findPendingConnectionBetween(User requester, User receiver){
        return connectionRepository.findByRequesterAndReceiverAndIsConnectedFalse(requester, receiver)
                .orElseThrow(() -> new NoSuchConnectionException(String.format("Failed to find a pending connection request by user: %s to user: %s", requester.getUsername(), receiver.getUsername())));
    }

    @Override
    public boolean pendingRequestExists(User requester, User receiver){
        return connectionRepository.existsByRequesterAndReceiverAndIsConnectedFalse(requester, receiver);
    }

    @Override
    public boolean activeConnectionExists(User userA, User userB){

        User canonicalUserA = userA;
        User canonicalUserB = userB;

        if(userA.getId().compareTo(userB.getId()) < 0){
            canonicalUserA = userB;
            canonicalUserB = userA;
        }

        return connectionRepository.existsByUserAAndUserBAndIsConnectedTrue(canonicalUserA, canonicalUserB);
    }
}
