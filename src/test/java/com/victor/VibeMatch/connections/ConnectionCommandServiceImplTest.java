package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.exceptions.ConnectionPersistenceException;
import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionCommandServiceImplTest {

    @Mock
    private ConnectionRepository connectionRepository;

    @InjectMocks
    private ConnectionCommandServiceImpl connectionCommandService;

    private User userAlpha, userBeta, userGamma;
    private Connection connectionAlphaBeta;

    @BeforeEach
    void setUp() {
        // Arrange: Initialize mock users with specific UUIDs for predictable sorting
        userAlpha = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111")) // Smaller UUID
                .username("userAlpha").build();
        userBeta = User.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222")) // Larger UUID
                .username("userBeta").build();
        userGamma = User.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .username("userGamma").build();

        connectionAlphaBeta = Connection.builder()
                .id(UUID.randomUUID())
                .userA(userAlpha)
                .userB(userBeta)
                .requester(userAlpha)
                .receiver(userBeta)
                .isConnected(false)
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    @DisplayName("Should successfully save a new connection")
    void saveConnection_success() {
        // Arrange
        when(connectionRepository.save(connectionAlphaBeta)).thenReturn(connectionAlphaBeta);

        // Act
        Connection savedConnection = connectionCommandService.saveConnection(connectionAlphaBeta);

        // Assert
        assertNotNull(savedConnection);
        assertEquals(connectionAlphaBeta.getId(), savedConnection.getId());
        verify(connectionRepository, times(1)).save(connectionAlphaBeta);
    }

    @Test
    @DisplayName("Should throw ConnectionPersistenceException on DataIntegrityViolationException during save")
    void saveConnection_dataIntegrityViolation_shouldThrowException() {
        // Arrange
        when(connectionRepository.save(connectionAlphaBeta)).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        ConnectionPersistenceException thrown = assertThrows(ConnectionPersistenceException.class, () ->
                connectionCommandService.saveConnection(connectionAlphaBeta));

        assertEquals("An data integrity error occurred while trying to save a connection", thrown.getMessage());
        verify(connectionRepository, times(1)).save(connectionAlphaBeta);
    }

    @Test
    @DisplayName("Should throw ConnectionPersistenceException on generic Exception during save")
    void saveConnection_genericException_shouldThrowException() {
        // Arrange
        when(connectionRepository.save(connectionAlphaBeta)).thenThrow(RuntimeException.class);

        // Act & Assert
        ConnectionPersistenceException thrown = assertThrows(ConnectionPersistenceException.class, () ->
                connectionCommandService.saveConnection(connectionAlphaBeta));

        assertEquals("An unexpected error occurred while trying to save a connection", thrown.getMessage());
        verify(connectionRepository, times(1)).save(connectionAlphaBeta);
    }


    @Test
    @DisplayName("Should successfully delete a connection when users passed in canonical order")
    void deleteConnection_canonicalOrder_success() {
        // Arrange
        doNothing().when(connectionRepository).deleteByUserAAndUserB(userBeta, userAlpha);

        // Act
        connectionCommandService.deleteConnection(userBeta, userAlpha);

        // Assert
        verify(connectionRepository, times(1)).deleteByUserAAndUserB(userBeta, userAlpha);
    }

    @Test
    @DisplayName("Should successfully delete a connection when users passed in reverse order (via canonicalization)")
    void deleteConnection_reverseOrder_success() {
        // Arrange
        doNothing().when(connectionRepository).deleteByUserAAndUserB(userBeta, userAlpha);

        // Act
        connectionCommandService.deleteConnection(userAlpha, userBeta);

        // Assert
        verify(connectionRepository, times(1)).deleteByUserAAndUserB(userBeta, userAlpha);
    }

    @Test
    @DisplayName("Should throw ConnectionPersistenceException on DataIntegrityViolationException during delete")
    void deleteConnection_dataIntegrityViolation_shouldThrowException() {
        // Arrange
        doThrow(DataIntegrityViolationException.class)
                .when(connectionRepository).deleteByUserAAndUserB(any(User.class), any(User.class));

        // Act & Assert
        ConnectionPersistenceException thrown = assertThrows(ConnectionPersistenceException.class, () ->
                connectionCommandService.deleteConnection(userAlpha, userBeta));

        assertEquals("An data integrity error occurred while trying to delete a connection", thrown.getMessage());
        verify(connectionRepository, times(1)).deleteByUserAAndUserB(any(User.class), any(User.class));
    }

    @Test
    @DisplayName("Should throw ConnectionPersistenceException on generic Exception during delete")
    void deleteConnection_genericException_shouldThrowException() {
        // Arrange
        doThrow(RuntimeException.class)
                .when(connectionRepository).deleteByUserAAndUserB(any(User.class), any(User.class));

        // Act & Assert
        ConnectionPersistenceException thrown = assertThrows(ConnectionPersistenceException.class, () ->
                connectionCommandService.deleteConnection(userAlpha, userBeta));

        assertEquals("An unexpected error occurred while trying to delete a connection", thrown.getMessage());
        verify(connectionRepository, times(1)).deleteByUserAAndUserB(any(User.class), any(User.class));
    }
}