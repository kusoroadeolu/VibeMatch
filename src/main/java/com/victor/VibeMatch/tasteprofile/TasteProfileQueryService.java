package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.user.User;

import java.util.UUID;

public interface TasteProfileQueryService {
    TasteProfile findByUser(User user);

    TasteProfile findByUserId(UUID userId);
}
