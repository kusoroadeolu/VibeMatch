package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

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
    public void should_map_user_to_response_dto(){
        //Act
        UserResponseDto responseDto = userMapper.responseDto(user);

        //Assert
        assertAll(
                () -> assertNotNull(responseDto),
                () -> assertEquals(user.getUsername(), responseDto.username()),
                () -> assertEquals(user.getCountry(), responseDto.country()),
                () -> assertEquals(user.getSpotifyId(), responseDto.spotifyId())
        );
    }

}