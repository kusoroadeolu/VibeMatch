package com.victor.VibeMatch.auth.service;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthMapper {
    public LoginResponseDto loginResponseDto(String username, String refreshToken , String jwtToken){
        return new LoginResponseDto(
                username,
                refreshToken,
                jwtToken
        );
    }

    public TokenDto tokenDto(SpotifyTokenResponse tokenResponse){
        return new TokenDto(
                tokenResponse.getAccessToken(),
                tokenResponse.getTokenType(),
                LocalDateTime.now(),
                tokenResponse.getExpiresIn(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getScope()
        );
    }

}


