package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.user.User;

import java.util.List;

public interface TasteProfileCalculationService {
    List<TasteWrapper> calculateTopGenres(User user);

    List<TasteWrapper> calculateTopArtists(User user);

    double calculateMainStreamScore(User user);

    double calculateDiscoveryPattern(User user);
}
