package com.victor.VibeMatch.security;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @Mock
    private UserQueryServiceImpl userQueryService;

    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

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
    public void loadByUserByUsername_shouldLoadUserPrincipal_whenUserExistsByUsername(){
        //Arrange
        String username = "mock-name";

        //When
        when(userQueryService.findByUsername(username)).thenReturn(user);

        //Act
        UserDetails userPrincipal = customUserDetailsService.loadUserByUsername(username);

        //Assert
        assertAll(
                () -> assertNotNull(userPrincipal),
                () -> assertEquals(username, userPrincipal.getUsername())
        );
        verify(userQueryService, times(1)).findByUsername(username);

    }

    @Test
    public void loadByUserBySpotifyId_shouldLoadUserPrincipal_whenUserExistsBySpotifyId(){
        //Arrange
        String spotifyId = "mock-id";

        //When
        when(userQueryService.findBySpotifyId(spotifyId)).thenReturn(user);

        //Act
        UserPrincipal userPrincipal =  customUserDetailsService.loadUserBySpotifyId(spotifyId);

        //Assert
        assertAll(
                () -> assertNotNull(userPrincipal),
                () -> assertEquals(spotifyId, userPrincipal.getSpotifyId())
        );
        verify(userQueryService, times(1)).findBySpotifyId(spotifyId);

    }
}