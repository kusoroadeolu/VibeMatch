package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;


public interface CompatibilityScoreRepository extends JpaRepository<CompatibilityScore, CompatibilityKey> {
    CompatibilityScore findByKeyUserIdAndTargetUserId(UUID userId, UUID targetUserId);
    void deleteByKeyUserId(UUID userId);
}
