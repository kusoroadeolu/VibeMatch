package com.victor.VibeMatch.compatibility;

import java.util.UUID;

public interface CompatibilityScoreQueryService {
    CompatibilityScore findByUserIdAndTargetId(UUID userId, UUID targetId);

}
