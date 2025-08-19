package com.victor.VibeMatch.tasteprofile.impl;

import com.victor.VibeMatch.tasteprofile.TasteProfileCalculationService;
import com.victor.VibeMatch.tasteprofile.utils.TasteProfileUtils;
import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapperUtils;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.userartist.UserArtistQueryService;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrackQueryService;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrackQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TasteProfileCalculationServiceImpl implements TasteProfileCalculationService {

    private final TasteWrapperUtils tasteWrapperUtils;
    private final UserArtistQueryService userArtistQueryService;
    private final TasteProfileUtils tasteProfileUtils;

    /**
     * Calculates the top 3 genres for a user
     * @param user The user
     * @return A list of wrapper classes containing the name, count and percentage of a genre
     * */
    @Override
    public List<TasteWrapper> calculateTopGenres(User user){
        List<UserArtist> artists = user.getUserArtists();

        if(artists == null || artists.isEmpty()){
            log.info("User artist list is null or empty. Returning empty list");
            return Collections.emptyList();
        }

        long totalGenreCount = tasteProfileUtils.getAllGenres(artists).size();
        log.info("Total Genre Count: {}", totalGenreCount);

        Map<String, Integer> topGenres = tasteProfileUtils.getTopGenres(artists);

        List<TasteWrapper> tasteWrappers = new ArrayList<>();

        for(Map.Entry<String, Integer> entry: topGenres.entrySet()){
            String genreName = entry.getKey();
            int count = entry.getValue();
            float percentage = ((float) count /totalGenreCount) * 100;
            tasteWrappers.add(tasteWrapperUtils.buildTasteWrapper(genreName, percentage, count));
            log.info("Added genre taste wrapper. Genre name: {}, Percentage: {}, Count: {}", genreName, percentage, count);
        }

        return tasteWrappers;
    }

    /**
     * Calculates the top 3 artists for a user
     * @param user The user
     * @return A list of  wrapper classes containing the name and rank of an artist
     * */
    @Override
    public List<TasteWrapper> calculateTopArtists(User user){
        int limit = 3;
        List<UserArtist> artists = userArtistQueryService.findArtistsByUserOrderByRanking(user, limit);

        log.info("Found top {} artists for user: {}", artists.size(), user.getUsername());

        List<TasteWrapper> tasteWrappers = new ArrayList<>();

        for(UserArtist ua: artists){
            String name = ua.getName();
            int rank = ua.getRanking();
            tasteWrappers.add(tasteWrapperUtils.buildTasteWrapper(name, rank));
        }

        return tasteWrappers;
    }

    /**
     * Calculates the mainstream score of a user's tracks and artists.
     * Basically a weighted average of how popular a users tracks and artists are
     * @param user The user
     * @return the mainstream score
     * */
    @Override
    public double calculateMainStreamScore(User user){
        List<UserArtist> artists = user.getUserArtists();
        List<UserTopTrack> topTracks = user.getUserTopTracks();
        List<UserRecentTrack> recentTracks = user.getUserRecentTracks();

        double artistPopularityAvg = tasteProfileUtils.calculateArtistPopularityAvg(artists)/100;
        double topTrackPopularityAvg = tasteProfileUtils.calculateTopTrackPopularityAvg(topTracks)/100;
        double recentTrackPopularityAvg = tasteProfileUtils.calculateRecentTrackPopularityAvg(recentTracks)/100;
        log.info("Artist Avg: {}, Top Track Avg: {}, Recent Track Avg: {}", artistPopularityAvg, topTrackPopularityAvg, recentTrackPopularityAvg);

        return tasteProfileUtils.calculateMainStreamScore(artistPopularityAvg, topTrackPopularityAvg, recentTrackPopularityAvg);
    }


    /**
     * Calculates the discovery score of a user
     * Basically calculates the Jaccard similarity of the artists featured in a user's recent and top tracks
     * @param user The user
     * @return the mainstream score
     * */
    @Override
    public double calculateDiscoveryPattern(User user){
        List<UserTopTrack> topTracks = user.getUserTopTracks();
        List<UserRecentTrack> recentTracks = user.getUserRecentTracks();

        double overlap = tasteProfileUtils.calculateDiscoveryRate(topTracks, recentTracks);
        log.info("Overlap: {} for user: {}", overlap, user.getUsername());

        double discoveryRate = 1.0d - overlap;
        log.info("Discovery Rate: {} for user: {}", discoveryRate, user.getUsername());

        return discoveryRate;
    }


}
