package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.exceptions.NoSuchConnectionException;
import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionQueryServiceImplTest {

    @Mock
    private ConnectionRepository connectionRepository;

    @InjectMocks
    private ConnectionQueryServiceImpl connectionQueryService;

    private User userOne, userTwo, userThree;
    private Connection activeConnection, requestedConnection, receivedConnection;

    @BeforeEach
    void setUp() {
        userOne = User.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).username("userOne").build();
        userTwo = User.builder().id(UUID.fromString("22222222-2222-2222-2222-222222222222")).username("userTwo").build();
        userThree = User.builder().id(UUID.fromString("33333333-3333-3333-3333-333333333333")).username("userThree").build();

        activeConnection = Connection.builder()
                .id(UUID.randomUUID()).userA(userOne).userB(userTwo)
                .requester(userOne).receiver(userTwo).isConnected(true).createdAt(LocalDateTime.now()).build();

        requestedConnection = Connection.builder()
                .id(UUID.randomUUID()).userA(userOne).userB(userThree)
                .requester(userOne).receiver(userThree).isConnected(false).createdAt(LocalDateTime.now()).build();

        receivedConnection = Connection.builder()
                .id(UUID.randomUUID()).userA(userTwo).userB(userThree)
                .requester(userTwo).receiver(userThree).isConnected(false).createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Should find all active connections for a given user")
    void findAllActiveConnections_shouldReturnList() {
        // Arrange
        when(connectionRepository.findByRequesterOrReceiverAndIsConnectedTrue(userOne, userOne))
                .thenReturn(List.of(activeConnection));

        // Act
        List<Connection> foundConnections = connectionQueryService.findAllActiveConnections(userOne);

        // Assert
        assertNotNull(foundConnections);
        assertFalse(foundConnections.isEmpty());
        assertEquals(1, foundConnections.size());
        assertTrue(foundConnections.contains(activeConnection));
        verify(connectionRepository).findByRequesterOrReceiverAndIsConnectedTrue(userOne, userOne);
    }

    @Test
    @DisplayName("Should find all pending connections sent by the user")
    void findPendingSentConnections_shouldReturnList() {
        // Arrange
        when(connectionRepository.findByRequesterAndIsConnectedFalse(userOne))
                .thenReturn(List.of(requestedConnection));

        // Act
        List<Connection> foundRequests = connectionQueryService.findPendingSentConnections(userOne);

        // Assert
        assertNotNull(foundRequests);
        assertFalse(foundRequests.isEmpty());
        assertEquals(1, foundRequests.size());
        assertTrue(foundRequests.contains(requestedConnection));
        verify(connectionRepository).findByRequesterAndIsConnectedFalse(userOne);
    }

    @Test
    @DisplayName("Should find all pending connections received by the user")
    void findPendingReceivedConnections_shouldReturnList() {
        // Arrange
        when(connectionRepository.findByReceiverAndIsConnectedFalse(userThree))
                .thenReturn(List.of(requestedConnection, receivedConnection));

        // Act
        List<Connection> foundRequests = connectionQueryService.findPendingReceivedConnections(userThree);

        // Assert
        assertNotNull(foundRequests);
        assertFalse(foundRequests.isEmpty());
        assertEquals(2, foundRequests.size());
        assertTrue(foundRequests.contains(requestedConnection));
        assertTrue(foundRequests.contains(receivedConnection));
        verify(connectionRepository).findByReceiverAndIsConnectedFalse(userThree);
    }

    @Test
    @DisplayName("Should find an active connection between two users regardless of input order")
    void findConnection_shouldReturnConnection_whenFound() {
        // Arrange
        when(connectionRepository.findByUserAAndUserBAndIsConnectedTrue(any(User.class), any(User.class)))
                .thenReturn(Optional.of(activeConnection));

        // Act
        Connection foundConnection = connectionQueryService.findConnection(userTwo, userOne);

        // Assert
        assertNotNull(foundConnection);
        assertEquals(activeConnection.getId(), foundConnection.getId());
        verify(connectionRepository).findByUserAAndUserBAndIsConnectedTrue(any(User.class), any(User.class));
    }

    @Test
    @DisplayName("Should throw NoSuchConnectionException when no active connection is found")
    void findConnection_shouldThrowException_whenNotFound() {
        // Arrange
        when(connectionRepository.findByUserAAndUserBAndIsConnectedTrue(any(User.class), any(User.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchConnectionException.class, () ->
                connectionQueryService.findConnection(userOne, userThree));

        verify(connectionRepository).findByUserAAndUserBAndIsConnectedTrue(any(User.class), any(User.class));
    }

    @Test
    @DisplayName("Should find a pending connection between two users")
    void findPendingConnectionBetween_shouldReturnConnection_whenFound() {
        // Arrange
        when(connectionRepository.findByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree)).thenReturn(Optional.of(requestedConnection));

        // Act
        Connection connection = connectionQueryService.findPendingConnectionBetween(userOne, userThree);

        // Assert
        assertEquals(requestedConnection, connection);
        verify(connectionRepository, times(1)).findByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree);
    }

    @Test
    @DisplayName("Should throw NoSuchConnectionException when a pending connection is not found")
    void findPendingConnectionBetween_shouldThrowException_whenNotFound() {
        // Arrange
        when(connectionRepository.findByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchConnectionException.class, () -> {
            connectionQueryService.findPendingConnectionBetween(userOne, userThree);
        });
        verify(connectionRepository, times(1)).findByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree);
    }

    @Test
    @DisplayName("Should return true if a pending request exists")
    void pendingRequestExists_shouldReturnTrue_whenExists() {
        // Arrange
        when(connectionRepository.existsByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree)).thenReturn(true);

        // Act
        boolean exists = connectionQueryService.pendingRequestExists(userOne, userThree);

        // Assert
        assertTrue(exists);
        verify(connectionRepository, times(1)).existsByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree);
    }

    @Test
    @DisplayName("Should return false if a pending request does not exist")
    void pendingRequestExists_shouldReturnFalse_whenNotExists() {
        // Arrange
        when(connectionRepository.existsByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree)).thenReturn(false);

        // Act
        boolean exists = connectionQueryService.pendingRequestExists(userOne, userThree);

        // Assert
        assertFalse(exists);
        verify(connectionRepository, times(1)).existsByRequesterAndReceiverAndIsConnectedFalse(userOne, userThree);
    }
}