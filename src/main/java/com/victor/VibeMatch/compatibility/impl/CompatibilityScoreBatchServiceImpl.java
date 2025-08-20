package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScorePersistenceService;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompatibilityScoreBatchServiceImpl implements com.victor.VibeMatch.compatibility.CompatibilityScoreBatchService {

    private final UserQueryService userQueryService;
    private final CompatibilityScorePersistenceService compatibilityScorePersistenceService;

    /**
     * Attempts to find a list of compatible users for a user
     * @param user The user for which we are finding the compatible users
     * @return A list of dtos with compatible users
     * */
    @Override
    public List<CompatibilityScoreResponseDto> returnAllCompatibleUsers(User user, List<User> targetUsers){
        //Clear all previous compatibility scores before recalculating
        compatibilityScorePersistenceService.deleteCompatibilityScoresByUser(user);

        List<CompatibilityScoreResponseDto> compatibilityScores = new ArrayList<>();

        for(User targetUser: targetUsers){
            UUID targetUserId = targetUser.getId();

            if(!targetUser.isPublic() || targetUserId.equals(user.getId()))continue;
            CompatibilityScoreResponseDto score =
                    compatibilityScorePersistenceService.saveCompatibilityScore(user, targetUser);
            compatibilityScores.add(score);

        }

        log.info("Found {} compatible users for user with ID: {}", compatibilityScores.size(), user.getId());
        compatibilityScores.sort(Comparator.comparing(CompatibilityScoreResponseDto::tasteCompatibilityScore)
                .thenComparing(CompatibilityScoreResponseDto::discoveryCompatibilityScore));
        return compatibilityScores;
    }

    @Override
    public List<CompatibilityScoreResponseDto> findCompatibleUsersInBatch(UUID userId){
        User user = userQueryService.findByUserId(userId);
        List<User> targetUsers = userQueryService.findAllUsers();
        return returnAllCompatibleUsers(user, targetUsers);
    }







}
