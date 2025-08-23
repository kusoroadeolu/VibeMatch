package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TasteProfileQueryServiceImpl implements TasteProfileQueryService {

    private final TasteProfileRepository tasteProfileRepository;

    @Override
    public TasteProfile findByUser(User user){
        log.info("Attempting to find taste profile for user: {}", user.getUsername());
        TasteProfile tasteProfile = tasteProfileRepository.findByUser(user);
        log.info("Successfully found taste profile for user: {}", user.getUsername());
        return tasteProfile;

    }

    @Override
    public TasteProfile findByUserId(UUID userId){
        log.info("Attempting to find taste profile for user with ID: {}", userId);
        TasteProfile tasteProfile = tasteProfileRepository.findByUserId(userId);
        log.info("Successfully found taste profile for user with ID: {}", userId);
        return tasteProfile;

    }

}
