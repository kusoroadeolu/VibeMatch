package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.user.User;

public interface CompatibilityScoreCommandService {
    CompatibilityScore saveCompatibilityScore(CompatibilityScore compatibilityScore);

    void deleteCompatibilityScoresByUser(User user);

    void deleteByUserAndTargetUser(User user, User targetUser);
}
