package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.user.User;

import java.util.List;
import java.util.UUID;

public interface CompatibilityScoreBatchService {
    List<CompatibilityScoreResponseDto> returnAllCompatibleUsers(User user, List<User> users);

    List<CompatibilityScoreResponseDto> findCompatibleUsersInBatch(UUID userId);
}
