package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityKey;
import com.victor.VibeMatch.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;


public interface CompatibilityScoreRepository extends JpaRepository<CompatibilityScore, CompatibilityKey> {
    void deleteByKeyUserId(UUID userId);

    void deleteByUserAndTargetUser(User user, User targetUser);

    boolean existsByUserAndTargetUser(User user, User targetUser);

    CompatibilityScore findByUserAndTargetUser(User user, User targetUser);
}
