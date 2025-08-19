package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScoreQueryService;
import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScoreRepository;
import com.victor.VibeMatch.exceptions.NoSuchUserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityScoreScoreQueryServiceImpl implements CompatibilityScoreQueryService {

    private final CompatibilityScoreRepository compatibilityScoreRepository;

    @Override
    public CompatibilityScore findByUserIdAndTargetId(UUID userId, UUID targetId) {
        if(userId == null || targetId == null){
            log.error("User ID or Target ID cannot be null");
            throw new NoSuchUserException("User ID or Target ID cannot be null");
        }

        log.info("Attempting to find compatibility score for user with ID: {}. Target User Id: {}", userId, targetId);
        CompatibilityScore score = compatibilityScoreRepository.findByKeyUserIdAndTargetUserId(userId, targetId);
        log.info("Successfully found compatibility score for user with ID: {}. Target User Id: {}", userId, targetId);
        return score;
    }
}
