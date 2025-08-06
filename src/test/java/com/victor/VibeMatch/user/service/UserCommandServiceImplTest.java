package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.exceptions.UserSaveException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    private User user;

    @BeforeEach
    public void setUp(){
        user = User
                .builder()
                .build();

    }


    @Test
    public void should_save_user(){
        //Arrange

        //When
        when(userRepository.save(any(User.class))).thenReturn(user);

        //Act
        User mockedUser = userCommandService.saveUser(user);

        //Assert
        verify(userRepository, times(1)).save(user);
        assertAll(
                () -> assertNotNull(mockedUser),
                () -> assertEquals(user, mockedUser)
        );
    }

    @Test
    public void saveUser_onIntegrityException_shouldThrowUserSaveException(){
        //When
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(""));

        //Act and Assert
        var ex = assertThrows(UserSaveException.class, () -> {
            userCommandService.saveUser(user);
        });
        assertEquals(ex.getMessage(), String.format("A data integrity error occurred while trying to save user: %s in the DB", user.getUsername()));
    }

    @Test
    public void should_update_refresh_token(){
        //Arrange
        String refreshToken = "mock-refresh-token";

        //When
        when(userRepository.save(any(User.class))).thenReturn(user);

        //Act
        userCommandService.updateRefreshToken(user, refreshToken);

        //Assert
        verify(userRepository, times(1)).save(user);
    }
}