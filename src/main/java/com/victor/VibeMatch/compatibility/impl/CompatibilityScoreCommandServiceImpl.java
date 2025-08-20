package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScoreRepository;
import com.victor.VibeMatch.exceptions.CompatibilityScoreDeleteException;
import com.victor.VibeMatch.exceptions.CompatibilityScoreSaveException;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CompatibilityScoreCommandServiceImpl implements com.victor.VibeMatch.compatibility.CompatibilityScoreCommandService {

    private final CompatibilityScoreRepository compatibilityScoreRepository;

    @Override
    public CompatibilityScore saveCompatibilityScore(CompatibilityScore compatibilityScore){
        try{
            log.info("Attempting to save compatibility score higher than desired thresholds");
            var score = compatibilityScoreRepository.save(compatibilityScore);

            log.info("Successfully saved compatibility score");
            return score;
        }catch (Exception e){
            log.error("An error occurred while trying to save the compatibility score", e);
            throw new CompatibilityScoreSaveException("An error occurred while trying to save the compatibility score", e);
        }
    }

    @Override
    public void deleteCompatibilityScoresByUser(User user){
        try{
            log.info("Attempting to delete all compatibility scores for user: {}", user.getUsername());
            compatibilityScoreRepository.deleteByKeyUserId(user.getId());
            log.info("Successfully deleted all compatibility scores for user: {}", user.getUsername());
        }catch (Exception e){
            log.info("An error occurred while trying to delete all compatibility scores for user: {}", user.getUsername());
            throw new CompatibilityScoreDeleteException(String.format("An error occurred while trying to delete all compatibility scores for user: %s", user.getUsername()));
        }
    }

    @Override
    public void deleteByUserAndTargetUser(User user, User targetUser) {
        compatibilityScoreRepository.deleteByUserAndTargetUser(user, targetUser);
    }
}
