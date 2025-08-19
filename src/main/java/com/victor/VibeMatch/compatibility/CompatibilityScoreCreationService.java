package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.user.User;

import java.util.List;

public interface CompatibilityScoreCreationService {
    List<CompatibilityWrapper> getSharedArtists(User user, User targetUser);

    List<CompatibilityWrapper> getSharedGenres(User user, User targetUser);

    double getDiscoveryCompatibility(User user, User targetUser);

    double getTasteCompatibility(User user, User targetUser);

    List<String> buildCompatibilityReasons(List<CompatibilityWrapper> sharedArtists, List<CompatibilityWrapper> sharedGenres, double discoveryCompatibility);
}
