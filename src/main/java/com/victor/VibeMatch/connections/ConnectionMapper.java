package com.victor.VibeMatch.connections;

import org.springframework.stereotype.Service;

@Service
public class ConnectionMapper {
    public ActiveConnectionResponseDto toActiveResponseDto(Connection connection){
        return new ActiveConnectionResponseDto(
                connection.getRequester().getUsername(),
                connection.getReceiver().getUsername(),
                connection.getRequester().getId(),
                connection.getReceiver().getId(),
                connection.isConnected(),
                connection.getConnectedSince()
        );
    }

    public InactiveConnectionResponseDto toInActiveResponseDto(Connection connection){
        return new InactiveConnectionResponseDto(
                connection.getRequester().getUsername(),
                connection.getReceiver().getUsername(),
                connection.getRequester().getId(),
                connection.getReceiver().getId(),
                connection.isConnected(),
                connection.getCreatedAt()
        );
    }

}
