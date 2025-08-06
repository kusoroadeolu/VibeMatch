package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthMapperTest {

    @InjectMocks
    private AuthMapper authMapperMock;

    @Test
    void loginResponseDto_givenCredentials_mapsFieldsCorrectly() {
        // Arrange
        String username = "testUser";
        String refreshToken = "mockRefreshToken";
        String jwtToken = "mockJwtToken";

        // Act
        LoginResponseDto mappedDto = authMapperMock.loginResponseDto(username, refreshToken, jwtToken);

        // Assert
        assertAll(
                () -> assertNotNull(mappedDto),
                () -> assertEquals(username, mappedDto.username()),
                () -> assertEquals(refreshToken, mappedDto.refreshToken()),
                () -> assertEquals(jwtToken, mappedDto.jwtToken())
        );
    }
}