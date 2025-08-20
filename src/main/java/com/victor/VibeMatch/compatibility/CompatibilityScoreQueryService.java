package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.user.User;

import java.util.UUID;

public interface CompatibilityScoreQueryService {
    CompatibilityScore findByUserAndTargetUser(User user, User targetUser);

    boolean existsByUserAndTargetUser(User user, User targetUser);
}
