package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.exceptions.NoSuchUserException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserRepository;
import com.victor.VibeMatch.user.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
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
class UserQueryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserQueryServiceImpl userQueryService;


    private User user;

    @BeforeEach
    public void setUp(){
        user = User
                .builder()
                .username("mock-name")
                .email("mock-email")
                .country("mock-country")
                .spotifyId("mock-id")
                .build();
    }

    @Test
    public void findByUsername_shouldReturnUser_whenUserExists(){
        //Arrange
        String username = "mock-name";

        //When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        //Act
        User mockUser = userQueryService.findByUsername(username);

        //Assert
        assertAll(
                () -> assertNotNull(mockUser),
                () -> assertEquals(username, mockUser.getUsername())
        );

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    public void findByUsername_shouldThrowNoSuchUserException_whenUserDoesNotExist(){
        //Arrange
        String username = "mock-name";

        //When
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        //Act and Assert
        var ex = assertThrows(NoSuchUserException.class, () -> {
            userQueryService.findByUsername(username);
        });
        assertEquals(ex.getMessage(), String.format("Failed to find user: %s in the DB", username));
    }

    @Test
    public void getUserData_shouldReturnUserData_whenGivenUsername(){
        //Arrange
        String spotifyId = "mock-id";
        var responseDto = new UserResponseDto("", "", "", spotifyId);

        //When
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userMapper.responseDto(user)).thenReturn(responseDto);
        
        //Act
        UserResponseDto mockDto = userQueryService.getUserData(spotifyId);
        
        //Assert
        assertAll(
                () -> assertNotNull(mockDto),
                () -> assertEquals(spotifyId, mockDto.spotifyId())
        );
        verify(userRepository, times(1)).findBySpotifyId(spotifyId);
        verify(userMapper, times(1)).responseDto(user);


    }

    @Test
    public void getUserData_shouldReturnUserData_whenGivenSpotifyId(){
        //Arrange
        String spotifyId = "mock-id";
        var responseDto = new UserResponseDto("mock-name", "mock-email", "mock-country", spotifyId);

        //When
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userMapper.responseDto(user)).thenReturn(responseDto);

        //Act
        UserResponseDto mockDto = userQueryService.getUserData(spotifyId);

        //Assert
        assertAll(
                () -> assertNotNull(mockDto),
                () -> assertEquals(spotifyId, mockDto.spotifyId())
        );
        verify(userRepository, times(1)).findBySpotifyId(spotifyId);
        verify(userMapper, times(1)).responseDto(user);
    }

    @Test
    public void getUserData_shouldThrowNoSuchUserException_whenUserDoesNotExist(){
        //Arrange
        String spotifyId = "non-existent-id";

        //When
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        //Act and Assert
        var ex = assertThrows(NoSuchUserException.class, () -> {
            userQueryService.getUserData(spotifyId);
        });
        assertEquals(String.format("Failed to find user with spotify ID: %s in the DB", spotifyId), ex.getMessage());

        verify(userRepository, times(1)).findBySpotifyId(spotifyId);
        verify(userMapper, never()).responseDto(any());
    }

    @Test
    public void findBySpotifyId_shouldReturnUser_whenUserExists(){
        // Arrange
        String spotifyId = "mock-id";

        // When
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));

        // Act
        User foundUser = userQueryService.findBySpotifyId(spotifyId);

        // Assert
        assertAll(
                () -> assertNotNull(foundUser),
                () -> assertEquals(spotifyId, foundUser.getSpotifyId())
        );

        verify(userRepository, times(1)).findBySpotifyId(spotifyId);
    }

    @Test
    public void findBySpotifyId_shouldThrowNoSuchUserException_whenUserDoesNotExist(){
        // Arrange
        String spotifyId = "non-existent-id";

        // When
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        // Act and Assert
        var ex = assertThrows(NoSuchUserException.class, () -> {
            userQueryService.findBySpotifyId(spotifyId);
        });
        assertEquals(String.format("Failed to find user with spotify ID: %s in the DB", spotifyId), ex.getMessage());

        verify(userRepository, times(1)).findBySpotifyId(spotifyId);
    }

    @Test
    public void existsBySpotifyId_shouldReturnTrue_whenUserExists(){
        // Arrange
        String spotifyId = "mock-id";
        when(userRepository.existsBySpotifyId(spotifyId)).thenReturn(true);

        // Act
        boolean exists = userQueryService.existsBySpotifyId(spotifyId);

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).existsBySpotifyId(spotifyId);
    }

    @Test
    public void existsBySpotifyId_shouldReturnFalse_whenUserDoesNotExist(){
        // Arrange
        String spotifyId = "non-existent-id";
        when(userRepository.existsBySpotifyId(spotifyId)).thenReturn(false);

        // Act
        boolean exists = userQueryService.existsBySpotifyId(spotifyId);

        // Assert
        assertFalse(exists);
        verify(userRepository, times(1)).existsBySpotifyId(spotifyId);
    }

    @Test
    public void findByUserId_shouldReturnUser_ifUserExists(){
        //Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //Act
        User mockUser = userQueryService.findByUserId(userId);

        //Assert
        assertNotNull(mockUser);
        assertEquals(user, mockUser);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void findByUserId_shouldThrowUserNotFoundEx_ifUserDoesNotExist(){
        //Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(NoSuchUserException.class, () -> {
           userQueryService.findByUserId(userId);
        });
    }

    @Test
    public void findAllUsers_shouldReturnAListOfAllUsersInTheDB(){
        //Arrange
        List<User> users = List.of(User.builder().username("user1").build(), User.builder().username("user2").build());
        when(userRepository.findAll()).thenReturn(users);

        //Act
        List<User> foundUsers = userQueryService.findAllUsers();

        //Assert
        assertNotNull(foundUsers);
        assertEquals(users.getFirst().getUsername(), foundUsers.getFirst().getUsername());
        assertEquals(users.get(1).getUsername(), foundUsers.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void findByLastSyncedByBefore_givenThreshold_shouldReturnAListOfUsersSyncedLastSyncedAtBeforeTheThreshold(){
        //Arrange
        LocalDateTime threshold = LocalDateTime.now().minusDays(12);
        List<User> users = List.of(User.builder().username("user1").lastSyncedAt(LocalDateTime.now().minusDays(2)).build(), User.builder().username("user2").lastSyncedAt(LocalDateTime.now().minusDays(3)).build());
        when(userRepository.findByLastSyncedAtBefore(threshold)).thenReturn(users);

        //Act
        List<User> foundUsers = userQueryService.findByLastSyncedAtBefore(threshold);

        //Assert
        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        assertEquals(users.getFirst().getUsername(), foundUsers.getFirst().getUsername());
        assertEquals(users.get(1).getUsername(), foundUsers.get(1).getUsername());
        verify(userRepository, times(1)).findByLastSyncedAtBefore(threshold);

    }

    @Test
    public void findByLastSyncedAtBefore_givenThreshold_shouldReturnAnEmptyList(){
        //Arrange
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        when(userRepository.findByLastSyncedAtBefore(threshold)).thenReturn(List.of());

        //Act
        List<User> foundUsers = userQueryService.findByLastSyncedAtBefore(threshold);

        //Assert
        assertNotNull(foundUsers);
        assertEquals(0, foundUsers.size());
        verify(userRepository, times(1)).findByLastSyncedAtBefore(threshold);

    }

}