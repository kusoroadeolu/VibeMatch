package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.user.User;

import java.util.UUID;

public interface CompatibilityScorePersistenceService {
    CompatibilityScoreResponseDto getCompatibilityScore(UUID userId, UUID targetUserId);


    CompatibilityScoreResponseDto saveCompatibilityScore(User user, User targetUser);

    void deleteCompatibilityScoresByUser(User user);

    CompatibilityScore buildCompatibilityScore(User user, User targetUser);
}
