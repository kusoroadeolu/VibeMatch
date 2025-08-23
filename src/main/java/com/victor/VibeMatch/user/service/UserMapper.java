package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public UserResponseDto responseDto(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCountry(),
                user.getSpotifyId(),
                user.getImageUrl()
        );
    }
}
