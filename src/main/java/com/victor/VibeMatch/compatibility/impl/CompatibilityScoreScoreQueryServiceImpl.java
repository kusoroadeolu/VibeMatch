package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScoreQueryService;
import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScoreRepository;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityScoreScoreQueryServiceImpl implements CompatibilityScoreQueryService {

    private final CompatibilityScoreRepository compatibilityScoreRepository;

    @Override
    public CompatibilityScore findByUserAndTargetUser(User user, User targetUser) {

        log.info("Attempting to find compatibility score for user  {}. Target User  {}", user.getUsername(), targetUser.getUsername());
        CompatibilityScore score = compatibilityScoreRepository.findByUserAndTargetUser(user, targetUser);
        log.info("Successfully found compatibility score for user: {}. Target User Id: {}", user.getUsername(), targetUser.getUsername());
        return score;
    }

    @Override
    public boolean existsByUserAndTargetUser(User user, User targetUser) {
       return compatibilityScoreRepository.existsByUserAndTargetUser(user, targetUser);

    }
}
