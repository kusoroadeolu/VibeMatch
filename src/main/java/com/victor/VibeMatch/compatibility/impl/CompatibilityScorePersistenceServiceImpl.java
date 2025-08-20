package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.*;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.exceptions.CompatibilityScorePersistenceException;
import com.victor.VibeMatch.exceptions.UserPrivacyException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompatibilityScorePersistenceServiceImpl implements CompatibilityScorePersistenceService {

    private final CompatibilityScoreCreationService compatibilityScoreCreationService;
    private final CompatibilityScoreCommandService compatibilityScoreCommandService;
    private final UserQueryService userQueryService;
    private final CompatibilityScoreMapper compatibilityScoreMapper;

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 2000, multiplier = 2))
    public CompatibilityScoreResponseDto getCompatibilityScore(UUID userId, UUID targetUserId){
        User user = userQueryService.findByUserId(userId);
        User targetUser = userQueryService.findByUserId(targetUserId);
        compatibilityScoreCommandService.deleteByUserAndTargetUser(user, targetUser);
        return saveCompatibilityScore(user, targetUser);
    }

    /**
     * Saves the compatibility score for two users
     * @param user The ID of the user
     * @param targetUser The ID of the target user
     * */
    @Override
    public CompatibilityScoreResponseDto saveCompatibilityScore(User user, User targetUser){
        if(!targetUser.isPublic()){
            log.info("User: {} profile is private. Cannot calculate compatibility score!", targetUser.getUsername());
            throw new UserPrivacyException(String.format("User: %s profile is private. Cannot calculate compatibility score!", targetUser.getUsername()));
        }

        CompatibilityScore score = buildCompatibilityScore(user, targetUser);

        if(score.getDiscoveryCompatibility() >= 0.6 && score.getTasteCompatibility() >= 0.7){
             compatibilityScoreCommandService.saveCompatibilityScore(score);
             log.info("Successfully saved compatibility score for user: {} and target user: {}", user.getUsername(), targetUser.getUsername());
        }

        return compatibilityScoreMapper.responseDto(score);
    }

    /**
     * Deletes all compatibility scores for a user
     * @param user The user
     * */
    @Override
    public void deleteCompatibilityScoresByUser(User user){
        compatibilityScoreCommandService.deleteCompatibilityScoresByUser(user);
        log.info("Successfully deleted compatibility scores for user: {}", user.getUsername());
    }


    @Override
    public CompatibilityScore buildCompatibilityScore(User user, User targetUser) {
        List<CompatibilityWrapper> sharedArtists = compatibilityScoreCreationService.getSharedArtists(user, targetUser);
        List<CompatibilityWrapper> sharedGenres = compatibilityScoreCreationService.getSharedGenres(user, targetUser);
        double discoveryCompatibility = compatibilityScoreCreationService.getDiscoveryCompatibility(user, targetUser);
        double tasteCompatibility = compatibilityScoreCreationService.getTasteCompatibility(user, targetUser);
        List<String> listeningReasons = compatibilityScoreCreationService.buildCompatibilityReasons(sharedArtists, sharedGenres, discoveryCompatibility);


        return CompatibilityScore
                .builder()
                .user(user)
                .targetUser(targetUser)
                .tasteCompatibility(tasteCompatibility)
                .sharedGenres(sharedGenres)
                .sharedArtists(sharedArtists)
                .compatibilityReasons(listeningReasons)
                .discoveryCompatibility(discoveryCompatibility)
                .lastCalculated(LocalDateTime.now())
                .build();
    }




}
