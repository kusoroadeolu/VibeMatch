package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConnectionMapperTest {

    @InjectMocks
    private ConnectionMapper connectionMapper;

    private Connection connection;
    private InactiveConnectionResponseDto inactiveConnectionResponseDto;

    @BeforeEach
    public void setUp(){
        LocalDateTime createdAt = LocalDateTime.now().minusDays(3);
        LocalDateTime connectedSince = LocalDateTime.now().minusDays(2);
        connection = Connection
                .builder()
                .requester(User.builder().username("user1").build())
                .receiver(User.builder().username("user2").build())
                .isConnected(true)
                .createdAt(createdAt)
                .connectedSince(connectedSince)
                .build();
    }

    @Test
    void shouldMapToActiveResponseDto() {
        //Arrange
        ActiveConnectionResponseDto expected = new ActiveConnectionResponseDto(
                "user1",
                "user2",
                true,
                 connection.getConnectedSince()
        );

        //Act
        ActiveConnectionResponseDto responseDto = connectionMapper.toActiveResponseDto(connection);

        //Assert
        assertNotNull(responseDto);
        assertEquals(expected.requestedBy(), responseDto.requestedBy());
        assertEquals(expected.sentTo(), responseDto.sentTo());
        assertEquals(expected.isConnected(), responseDto.isConnected());
        assertEquals(expected.connectedSince(), responseDto.connectedSince());
    }

    @Test
    void shouldMapToInActiveResponseDto() {
        //Arrange
        InactiveConnectionResponseDto expected = new InactiveConnectionResponseDto(
                "user1",
                "user2",
                true,
                connection.getCreatedAt()
        );

        //Act
        InactiveConnectionResponseDto responseDto = connectionMapper.toInActiveResponseDto(connection);

        //Assert
        assertNotNull(responseDto);
        assertEquals(expected.requestedBy(), responseDto.requestedBy());
        assertEquals(expected.sentTo(), responseDto.sentTo());
        assertEquals(expected.isConnected(), responseDto.isConnected());
        assertEquals(expected.sentAt(), responseDto.sentAt());
    }
}