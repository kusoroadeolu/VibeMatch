package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.userartist.UserArtist;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CompatibilityCalculationService {
    //Gets shared artists between users
    List<CompatibilityWrapper> getSharedArtists(Map<String, UserArtist> map1, Map<String, UserArtist> map2);

    //Gets shared genres
    List<CompatibilityWrapper> getSharedGenres(List<String> genres1, List<String> genres2);

    double calculateDiscoveryCompatibility(double discoveryScore1, double discoveryScore2, double mainstreamScore1, double mainstreamScore2);

    double calculateTasteCompatibility(List<UserArtist> artists1, List<UserArtist> artists2,
                                       List<UserArtist> filteredArtists1, List<UserArtist> filteredArtists2, int sharedArtistSize);

}
