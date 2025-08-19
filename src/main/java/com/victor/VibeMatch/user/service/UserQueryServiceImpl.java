package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.exceptions.NoSuchUserException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserRepository;
import com.victor.VibeMatch.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryServiceImpl implements UserQueryService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username){
        if(username == null){
            log.info("Username cannot be null");
            throw new NoSuchUserException("Username cannot be null");
        }

        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new NoSuchUserException(String.format("Failed to find user: %s in the DB", username)));
    }

    @Override
    public User findByUserId(UUID userId){
        if(userId == null){
            log.info("User ID cannot be null");
            throw new NoSuchUserException("User ID cannot be null");
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NoSuchUserException(String.format("Failed to find user with ID: %s in the DB", userId)));
    }

    @Override
    public UserResponseDto getUserData(String spotifyId){
        User user = findBySpotifyId(spotifyId);
        return userMapper.responseDto(user);
    }

    @Override
    public User findBySpotifyId(String spotifyId){
        if(spotifyId == null){
            log.info("Spotify ID cannot be null");
            throw new NoSuchUserException("Spotify ID cannot be null");
        }

        return userRepository
                .findBySpotifyId(spotifyId)
                .orElseThrow(() -> new NoSuchUserException(String.format("Failed to find user with spotify ID: %s in the DB", spotifyId)));
    }

    @Override
    public List<User> findAllUsers(){
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());
        return users;
    }

    @Override
    public List<User> findByLastSyncedAtBefore(LocalDateTime then) {
        List<User> users = userRepository.findByLastSyncedAtBefore(then);
        log.info("Found {} users who haven't been synced for 24 hours", users.size());
        return users;
    }

    @Override
    public boolean existsBySpotifyId(String spotifyId) {
        return userRepository.existsBySpotifyId(spotifyId);
    }
}
