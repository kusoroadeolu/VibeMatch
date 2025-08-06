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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

        // When
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

        // When
        when(userRepository.existsBySpotifyId(spotifyId)).thenReturn(false);

        // Act
        boolean exists = userQueryService.existsBySpotifyId(spotifyId);

        // Assert
        assertFalse(exists);
        verify(userRepository, times(1)).existsBySpotifyId(spotifyId);
    }
}