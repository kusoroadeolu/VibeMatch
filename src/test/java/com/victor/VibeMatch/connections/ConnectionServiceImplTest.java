package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ConnectionServiceImplTest {

    @Mock
    private ConnectionQueryService connectionQueryService;
    @Mock
    private ConnectionCommandService connectionCommandService;
    @Mock
    private ConnectionMapper connectionMapper;
    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private ConnectionServiceImpl connectionService;

    private User userAlpha, userBeta, userGamma;
    private Connection activeConnectionAB;
    private Connection pendingRequestAG;
    private Connection pendingRequestGA;

    private ActiveConnectionResponseDto activeDto;
    private InactiveConnectionResponseDto inactiveDto;
    private InactiveConnectionResponseDto inactiveDto2;
    private ConnectionWrapperResponseDto wrapperActiveDto;
    private ConnectionWrapperResponseDto wrapperInactiveDto;

    @BeforeEach
    void setUp() {
        // Arrange: Mock User objects with specific UUIDs for predictable canonicalization
        userAlpha = User.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).username("userAlpha").build();
        userBeta = User.builder().id(UUID.fromString("22222222-2222-2222-2222-222222222222")).username("userBeta").build();
        userGamma = User.builder().id(UUID.fromString("33333333-3333-3333-3333-333333333333")).username("userGamma").build();

        // Arrange: Mock Connection objects
        activeConnectionAB = Connection.builder().id(UUID.randomUUID())
                .userA(userBeta).userB(userAlpha)
                .requester(userAlpha).receiver(userBeta).isConnected(true).createdAt(LocalDateTime.now()).build();

        pendingRequestAG = Connection.builder().id(UUID.randomUUID())
                .userA(userGamma).userB(userAlpha)
                .requester(userAlpha)
                .receiver(userGamma).isConnected(false).createdAt(LocalDateTime.now()).build();

        pendingRequestGA = Connection.builder().id(UUID.randomUUID())
                .userA(userGamma).userB(userAlpha)
                .requester(userGamma).receiver(userAlpha).isConnected(false).createdAt(LocalDateTime.now()).build();

        // Arrange: Mock DTOs and their wrappers
        activeDto = new ActiveConnectionResponseDto("req", "rec", true, LocalDateTime.now());
        inactiveDto2 = new InactiveConnectionResponseDto("userAlpha", "userGamma", false, pendingRequestAG.getCreatedAt());
        inactiveDto = new InactiveConnectionResponseDto("req", "rec", false, LocalDateTime.now());

        wrapperActiveDto = new ConnectionWrapperResponseDto(activeDto, null);
        wrapperInactiveDto = new ConnectionWrapperResponseDto(null, inactiveDto);
    }

    @Test
    @DisplayName("Should find all active connections for a user")
    void findAllActiveConnections_shouldReturnList() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(connectionQueryService.findAllActiveConnections(userAlpha)).thenReturn(List.of(activeConnectionAB));
        when(connectionMapper.toActiveResponseDto(activeConnectionAB)).thenReturn(activeDto);

        // Act
        List<ActiveConnectionResponseDto> result = connectionService.findAllActiveConnections(userAlpha.getId());

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(activeDto, result.get(0));
        verify(userQueryService).findByUserId(userAlpha.getId());
        verify(connectionQueryService).findAllActiveConnections(userAlpha);
    }

    @Test
    @DisplayName("Should find all pending connections received by a user")
    void findPendingReceivedConnections_shouldReturnList() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(connectionQueryService.findPendingReceivedConnections(userAlpha)).thenReturn(List.of(pendingRequestGA));
        when(connectionMapper.toInActiveResponseDto(pendingRequestGA)).thenReturn(inactiveDto);

        // Act
        List<InactiveConnectionResponseDto> result = connectionService.findPendingReceivedConnections(userAlpha.getId());

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(inactiveDto, result.get(0));
        verify(userQueryService).findByUserId(userAlpha.getId());
        verify(connectionQueryService).findPendingReceivedConnections(userAlpha);
    }

    @Test
    @DisplayName("Should find all pending connections sent by a user")
    void findPendingSentConnections_shouldReturnList() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(connectionQueryService.findPendingSentConnections(userAlpha)).thenReturn(List.of(pendingRequestAG));
        when(connectionMapper.toInActiveResponseDto(pendingRequestAG)).thenReturn(inactiveDto);

        // Act
        List<InactiveConnectionResponseDto> result = connectionService.findPendingSentConnections(userAlpha.getId());

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(inactiveDto, result.get(0));
        verify(userQueryService).findByUserId(userAlpha.getId());
        verify(connectionQueryService).findPendingSentConnections(userAlpha);
    }

    @Test
    @DisplayName("Should find an active connection between two users")
    void findActiveConnectionBetweenTwoUsers_shouldReturnConnection() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(userQueryService.findByUserId(userBeta.getId())).thenReturn(userBeta);
        when(connectionQueryService.findConnection(userAlpha, userBeta)).thenReturn(activeConnectionAB);
        when(connectionMapper.toActiveResponseDto(activeConnectionAB)).thenReturn(activeDto);

        // Act
        ActiveConnectionResponseDto result = connectionService.findActiveConnectionBetweenTwoUsers(userAlpha.getId(), userBeta.getId());

        // Assert
        assertNotNull(result);
        assertEquals(activeDto, result);
        verify(userQueryService).findByUserId(userAlpha.getId());
        verify(userQueryService).findByUserId(userBeta.getId());
        verify(connectionQueryService).findConnection(userAlpha, userBeta);
    }

    @Test
    @DisplayName("requestConnection: Should return active DTO when connection already exists")
    void requestConnection_returnsActiveDto_whenActiveConnectionExists() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(userQueryService.findByUserId(userBeta.getId())).thenReturn(userBeta);
        when(connectionQueryService.activeConnectionExists(userBeta, userAlpha)).thenReturn(true);
        when(connectionQueryService.findConnection(userBeta, userAlpha)).thenReturn(activeConnectionAB);
        when(connectionMapper.toActiveResponseDto(activeConnectionAB)).thenReturn(activeDto);

        // Act
        ConnectionWrapperResponseDto result = connectionService.requestConnection(userAlpha.getId(), userBeta.getId());

        // Assert
        assertNotNull(result);
        assertNotNull(result.activeConnectionResponseDto());
        assertNull(result.inactiveConnectionResponseDto());
        assertEquals(activeDto, result.activeConnectionResponseDto());
        verify(connectionQueryService).activeConnectionExists(userBeta, userAlpha);
        verify(connectionQueryService, never()).findInactiveConnection(any(), any());
        verify(connectionCommandService, never()).saveConnection(any());
    }

    @Test
    @DisplayName("requestConnection: Should return active DTO for auto-accepted reciprocal request")
    void requestConnection_autoAcceptsAndReturnsActiveDto_whenReciprocalRequestExists() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(userQueryService.findByUserId(userGamma.getId())).thenReturn(userGamma);
        when(connectionQueryService.activeConnectionExists(any(), any())).thenReturn(false);
        when(connectionQueryService.findInactiveConnection(userGamma, userAlpha)).thenReturn(Optional.of(pendingRequestGA));

        // Mock the internal call to acceptConnection
        when(connectionQueryService.findPendingConnectionBetween(userGamma, userAlpha)).thenReturn(pendingRequestGA);
        when(connectionCommandService.saveConnection(any(Connection.class))).thenReturn(activeConnectionAB);
        when(connectionMapper.toActiveResponseDto(any(Connection.class))).thenReturn(activeDto);

        // Act
        ConnectionWrapperResponseDto result = connectionService.requestConnection(userAlpha.getId(), userGamma.getId());

        // Assert
        assertNotNull(result);
        assertNotNull(result.activeConnectionResponseDto());
        assertNull(result.inactiveConnectionResponseDto());
        assertEquals(activeDto, result.activeConnectionResponseDto());

        verify(connectionQueryService).findInactiveConnection(userGamma, userAlpha);
        verify(connectionCommandService).saveConnection(pendingRequestGA);
    }

    @Test
    @DisplayName("requestConnection: Should return inactive DTO for a newly created pending request")
    void requestConnection_returnsInactiveDto_forNewPendingRequest() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(userQueryService.findByUserId(userGamma.getId())).thenReturn(userGamma);
        when(connectionQueryService.activeConnectionExists(any(), any())).thenReturn(false);
        when(connectionQueryService.findInactiveConnection(any(), any())).thenReturn(Optional.empty());

        // Use ArgumentCaptor to capture the Connection object passed to saveConnection
        ArgumentCaptor<Connection> connectionCaptor = ArgumentCaptor.forClass(Connection.class);
        when(connectionCommandService.saveConnection(connectionCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Stub the mapper to accept ANY Connection object
        when(connectionMapper.toInActiveResponseDto(any(Connection.class))).thenReturn(inactiveDto);

        // Act
        ConnectionWrapperResponseDto result = connectionService.requestConnection(userAlpha.getId(), userGamma.getId());

        // Assert
        assertNotNull(result);
        assertNull(result.activeConnectionResponseDto());
        assertNotNull(result.inactiveConnectionResponseDto());
        assertEquals(inactiveDto, result.inactiveConnectionResponseDto());

        verify(connectionQueryService).activeConnectionExists(userGamma, userAlpha);
        verify(connectionQueryService).findInactiveConnection(userGamma, userAlpha);
        verify(connectionCommandService).saveConnection(any(Connection.class));
        verify(connectionMapper).toInActiveResponseDto(any(Connection.class));

        Connection capturedConnection = connectionCaptor.getValue();
        assertNotNull(capturedConnection);
        assertEquals(userGamma, capturedConnection.getUserA());
        assertEquals(userAlpha, capturedConnection.getUserB());
        assertEquals(userAlpha, capturedConnection.getRequester());
        assertEquals(userGamma, capturedConnection.getReceiver());
    }

    @Test
    @DisplayName("Should remove a connection between two users")
    void removeConnection_shouldDeleteConnection() {
        // Arrange
        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(userQueryService.findByUserId(userBeta.getId())).thenReturn(userBeta);
        doNothing().when(connectionCommandService).deleteConnection(userAlpha, userBeta);

        // Act
        connectionService.removeConnection(userAlpha.getId(), userBeta.getId());

        // Assert
        verify(userQueryService).findByUserId(userAlpha.getId());
        verify(userQueryService).findByUserId(userBeta.getId());
        verify(connectionCommandService, times(1)).deleteConnection(userAlpha, userBeta);
    }

    @Test
    @DisplayName("Should accept a connection and save it")
    void acceptConnection_shouldUpdateAndSaveConnection() {
        // Arrange
        Connection connectionToAccept = Connection.builder()
                .id(UUID.randomUUID())
                .requester(userAlpha).receiver(userBeta)
                .isConnected(false).build();

        when(userQueryService.findByUserId(userAlpha.getId())).thenReturn(userAlpha);
        when(userQueryService.findByUserId(userBeta.getId())).thenReturn(userBeta);
        when(connectionQueryService.findPendingConnectionBetween(userAlpha, userBeta)).thenReturn(connectionToAccept);
        when(connectionCommandService.saveConnection(connectionToAccept)).thenReturn(activeConnectionAB);
        when(connectionMapper.toActiveResponseDto(activeConnectionAB)).thenReturn(activeDto);

        // Act
        ActiveConnectionResponseDto result = connectionService.acceptConnection(userAlpha.getId(), userBeta.getId());

        // Assert
        assertNotNull(result);
        assertEquals(activeDto, result);
        assertTrue(connectionToAccept.isConnected());
        assertNotNull(connectionToAccept.getConnectedSince());
        verify(connectionCommandService).saveConnection(connectionToAccept);
        verify(connectionMapper).toActiveResponseDto(activeConnectionAB);
    }

    @Test
    @DisplayName("buildConnection: Should correctly build a new pending connection")
    void buildConnection_shouldBuildCorrectConnection() {
        // Act
        Connection builtConnection = connectionService.buildConnection(userBeta, userAlpha, userAlpha, userBeta);

        // Assert
        assertNotNull(builtConnection);
        assertEquals(userBeta, builtConnection.getUserA());
        assertEquals(userAlpha, builtConnection.getUserB());
        assertEquals(userAlpha, builtConnection.getRequester());
        assertEquals(userBeta, builtConnection.getReceiver());
    }
}