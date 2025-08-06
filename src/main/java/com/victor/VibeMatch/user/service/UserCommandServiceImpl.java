package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.exceptions.UserSaveException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCommandServiceImpl implements UserCommandService{
    private final UserRepository userRepository;

    @Override
    public User saveUser(User user){
        try{
            log.info("Attempting to save user: {} in the DB.", user.getUsername());
            var saved = userRepository.save(user);
            log.info("Successfully saved user: {} in the DB", user.getUsername());
            return saved;
        }catch (DataIntegrityViolationException e){
            log.error("A data integrity error occurred while trying to save user: {} in the DB", user.getUsername(), e);
            throw new UserSaveException(String.format("A data integrity error occurred while trying to save user: %s in the DB", user.getUsername()), e);
        }catch (Exception e){
            log.error("An unexpected error occurred while trying to save user: {} in the DB", user.getUsername(), e);
            throw new UserSaveException(String.format("An unexpected error occurred while trying to save user: %s in the DB", user.getUsername()), e);
        }
    }

    @Override
    public void updateRefreshToken(User user, String refreshToken){
        user.setRefreshToken(refreshToken);
        saveUser(user);
        log.info("Successfully updated refresh token for user: {}", user.getUsername());
    }
}
