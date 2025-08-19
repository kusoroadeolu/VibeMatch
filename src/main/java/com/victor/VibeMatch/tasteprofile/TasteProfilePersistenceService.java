package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.user.User;

import java.util.List;
import java.util.UUID;

public interface TasteProfilePersistenceService {
    TasteProfile createUserTasteProfile(UUID userId);

    TasteProfile buildTasteProfile(User user, List<TasteWrapper> genreWrapper, List<TasteWrapper> artistWrapper, double mainstreamScore, double discoveryPattern);
}
