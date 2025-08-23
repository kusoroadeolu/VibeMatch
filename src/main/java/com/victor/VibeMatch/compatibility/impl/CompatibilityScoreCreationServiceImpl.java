package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityCalculationService;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileQueryService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityScoreCreationServiceImpl implements com.victor.VibeMatch.compatibility.CompatibilityScoreCreationService {
    private final CompatibilityCalculationService compatibilityCalculationService;

    /**
     * Gets shared artists between both users
     * @param user The user
     * @param targetUser The target user to calculate compatibility with
     * @return A list of their shared artists.
     * */
    @Override

    public List<CompatibilityWrapper> getSharedArtists(User user, User targetUser){
        List<UserArtist> userArtists = user.getUserArtists();
        List<UserArtist> targetUserArtists = targetUser.getUserArtists();

        if(userArtists == null || targetUserArtists == null || userArtists.isEmpty() || targetUserArtists.isEmpty()){
            return Collections.emptyList();
        }

        Map<String, UserArtist> userMap = userArtists
                .stream()
                .collect(Collectors.toMap(UserArtist::getArtistSpotifyId, a -> a));
        Map<String, UserArtist> targetUserMap = targetUserArtists
                .stream()
                .collect(Collectors.toMap(UserArtist::getArtistSpotifyId, a -> a));

        Map<String, UserArtist> largerMap;
        Map<String, UserArtist> smallerMap;

        if(userMap.size() > targetUserMap.size()) {

            largerMap = userMap;

            smallerMap = targetUserMap;
        }else{
            largerMap = targetUserMap;
            smallerMap = userMap;

        }
        List<CompatibilityWrapper> compatibilityWrappers = compatibilityCalculationService.getSharedArtists(largerMap, smallerMap);
        log.info("Successfully calculated users top shared artists");
        return compatibilityWrappers;
    }


    /**
     * Gets shared genres between both users
     * @param user The user
     * @param targetUser The target user to calculate compatibility with
     * @return A list of their shared genres.
     * */
    @Override
    public List<CompatibilityWrapper> getSharedGenres(User user, User targetUser){
        List<UserArtist> userArtists = user.getUserArtists();
        List<UserArtist> targetUserArtists = targetUser.getUserArtists();

        if(userArtists == null || targetUserArtists == null || userArtists.isEmpty() || targetUserArtists.isEmpty()){
            return Collections.emptyList();
        }

        List<String> userArtistGenres = userArtists.stream()
                .flatMap(a -> a.getGenres().stream())
                .toList();
        List<String> targetUserArtistGenres = targetUserArtists
                .stream()
                .flatMap(a -> a.getGenres().stream())
                .toList();

        List<String> largerList;
        List<String> smallerList;

        if(userArtistGenres.size() > targetUserArtistGenres.size()){
            largerList = userArtistGenres;
            smallerList = targetUserArtistGenres;
        }else{
            largerList = targetUserArtistGenres;
            smallerList = userArtistGenres;
        }

        List<CompatibilityWrapper> compatibilityWrappers = compatibilityCalculationService.getSharedGenres(largerList, smallerList);
        log.info("Successfully calculated users top shared genres");
        return compatibilityWrappers;
    }

    /**
     * Calculates the discovery compatibility between two users. Their listening behavior
     * (If they tend to stick with certain artists, If they prefer popular artists)
     * @param user The user
     * @param targetUser The target user to calculate compatibility with
     * @return A decimal of their compatibility. A higher decimal means they're compatible and vice versa
     * */
    @Override
    public double getDiscoveryCompatibility(User user, User targetUser){
        TasteProfile userTasteProfile = user.getTasteProfile();
        TasteProfile targetUserTasteProfile = targetUser.getTasteProfile();

        double userDiscoveryPattern = userTasteProfile.getDiscoveryPattern();
        double targetUserDiscoveryPattern = targetUserTasteProfile.getDiscoveryPattern();
        double userMainstreamScore = userTasteProfile.getMainstreamScore();
        double targetUserMainstreamScore = targetUserTasteProfile.getMainstreamScore();

        log.info("User(Name: {}, Discovery Pattern: {}, Mainstream Score: {})", user.getUsername(), userDiscoveryPattern, userMainstreamScore);
        log.info("Target User(Name: {}, Discovery Pattern: {}, Mainstream Score: {})", targetUser.getUsername(), targetUserDiscoveryPattern, targetUserMainstreamScore);

        double behavioralCompatibility = compatibilityCalculationService.calculateDiscoveryCompatibility(userDiscoveryPattern, targetUserDiscoveryPattern, userMainstreamScore, targetUserMainstreamScore);
        log.info("Successfully calculated behavioral compatibility for user: {} and target user: {}. Behavioral Compatibility: {}", user.getUsername(), targetUser.getUsername(), behavioralCompatibility);
        return behavioralCompatibility;
    }

    /**
     * Calculates the taste compatibility between two users. Their listening tastes(Artists, Genres)
     * @param user The user
     * @param targetUser The target user to calculate compatibility with
     * @return A decimal of their compatibility. A higher decimal means they're compatible and vice versa
     * */
    @Override
    public double getTasteCompatibility(User user, User targetUser){
        List<UserArtist> userArtists = user.getUserArtists();
        List<UserArtist> targetUserArtists = targetUser.getUserArtists();

        if(userArtists == null || targetUserArtists == null || userArtists.isEmpty() || targetUserArtists.isEmpty()){
            return 0.0d;
        }

        Map<String, UserArtist> userMap = userArtists
                .stream()
                .collect(Collectors.toMap(UserArtist::getArtistSpotifyId, a -> a));

        Map<String, UserArtist> targetUserMap = targetUserArtists
                .stream()
                .collect(Collectors.toMap(UserArtist::getArtistSpotifyId, a -> a));

        Map<String, UserArtist> smallerMap = userMap.size() < targetUserMap.size() ? userMap : targetUserMap;
        Map<String, UserArtist> largerMap = userMap.size() < targetUserMap.size() ? targetUserMap : userMap;

        Set<String> sharedArtistsIds = new HashSet<>();

        for (String artistId : smallerMap.keySet()) {
            if (largerMap.containsKey(artistId)) {
                sharedArtistsIds.add(artistId);
            }
        }

        List<UserArtist> filteredArtists1 = new ArrayList<>();
        List<UserArtist> filteredArtists2 = new ArrayList<>();

        for(String artistId: sharedArtistsIds){
            filteredArtists1.add(userMap.get(artistId));
            filteredArtists2.add(targetUserMap.get(artistId));
        }
        
        double compatibility = compatibilityCalculationService.calculateTasteCompatibility(
                userArtists, targetUserArtists, filteredArtists1, filteredArtists2
        );
        log.info("Successfully calculated taste compatibility for user: {} and target user: {}. Behavioral Compatibility: {}", user.getUsername(), targetUser.getUsername(), compatibility);
        return compatibility;
    }

    /**
     * Builds a list of human-readable reasons explaining the compatibility between two users.
     *
     * @param sharedArtists A list of CompatibilityWrapper objects representing shared artists.
     * @param sharedGenres A list of CompatibilityWrapper objects representing shared genres.
     * @param discoveryCompatibility The calculated discovery pattern compatibility score.
     * @return A list of strings, each representing a compatibility reason.
     */
    @Override
    public List<String> buildCompatibilityReasons(List<CompatibilityWrapper> sharedArtists, List<CompatibilityWrapper> sharedGenres, double discoveryCompatibility) {
        List<String> reasons = new ArrayList<>();

        // Reason for shared artists
        if (sharedArtists != null && !sharedArtists.isEmpty()) {
            reasons.add("You have " + sharedArtists.size() + " artists in common.");
            if (sharedArtists.getFirst().getName() != null && !sharedArtists.getFirst().getName().isEmpty()) {
                reasons.add("You both enjoy " + sharedArtists.getFirst().getName() + ".");
            }
        }

        // Reason for shared genres
        if (sharedGenres != null && !sharedGenres.isEmpty()) {
            if (sharedGenres.getFirst().getName() != null && !sharedGenres.getFirst().getName().isEmpty()) {
                reasons.add("You both love " + sharedGenres.getFirst().getName() + ".");
            }
        }

        // Reason for discovery compatibility
        if (discoveryCompatibility > 0.8) {
            reasons.add("You have very similar music discovery patterns.");
        } else if (discoveryCompatibility > 0.6) {
            reasons.add("You have similar music discovery patterns.");
        } else if (discoveryCompatibility > 0.4) {
            reasons.add("You have somewhat similar music discovery patterns.");
        }

        return reasons;
    }

}
