package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.exceptions.NoSuchUserException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserRepository;
import com.victor.VibeMatch.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryServiceImpl implements UserQueryService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username){
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new NoSuchUserException(String.format("Failed to find user: %s in the DB", username)));
    }


    @Override
    public UserResponseDto getUserData(String spotifyId){
        User user = findBySpotifyId(spotifyId);
        return userMapper.responseDto(user);
    }

    @Override
    public User findBySpotifyId(String spotifyId){
        return userRepository
                .findBySpotifyId(spotifyId)
                .orElseThrow(() -> new NoSuchUserException(String.format("Failed to find user with spotify ID: %s in the DB", spotifyId)));
    }

    @Override
    public boolean existsBySpotifyId(String spotifyId) {
        return userRepository.existsBySpotifyId(spotifyId);
    }
}
