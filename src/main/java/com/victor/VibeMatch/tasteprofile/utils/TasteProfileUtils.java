package com.victor.VibeMatch.tasteprofile.utils;

import com.victor.VibeMatch.math.MathUtils;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TasteProfileUtils {

    private final MathUtils mathUtils;


    //Gets the top 3 genres in a list of user artists
    public Map<String, Integer> getTopGenres(List<UserArtist> artists){
        List<String> genres = getAllGenres(artists);

        Map<String, Integer> mapCountToKey = mathUtils.mapCountToKey(genres);
        log.info("Successfully mapped all genres to their count. Size: {}", mapCountToKey.size());

        return mapCountToTopString(mapCountToKey);
    }


    public List<String> getAllGenres(List<UserArtist> artists){
        return artists
                .stream()
                .flatMap(artist -> artist.getGenres().stream())
                .toList();
    }


    public Map<String, Integer> mapCountToTopString(Map<String, Integer> mapCountToKey){
        //Convert the map entry to a list for sorting
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(mapCountToKey.entrySet());
        //Sort in descending order
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        log.info("Successfully sorted all the genre counts in reverse order.");

        //Linked hash map to maintain order of insertion
        Map<String, Integer> mapTopGenres = new LinkedHashMap<>(3);

        int count = 0;
        for(Map.Entry<String, Integer> entry : entryList){
            int value = entry.getValue();
            String key = entry.getKey();

            if(count < 5){
                mapTopGenres.put(key, value);
                count++;
            }else{
                break;
            }
        }

        log.info("Successfully mapped top 5 genres to their count");
        return mapTopGenres;
    }

    public double calculateArtistPopularityAvg(List<UserArtist> artists){
        List<Integer> popularity = artists
                .stream()
                .map(UserArtist::getPopularity)
                .toList();
        return mathUtils.getAverage(popularity);
    }

    public double calculateTopTrackPopularityAvg(List<UserTopTrack> topTracks){
        List<Integer> popularity = topTracks
                .stream()
                .map(UserTopTrack::getPopularity)
                .toList();
        return mathUtils.getAverage(popularity);
    }

    public double calculateRecentTrackPopularityAvg(List<UserRecentTrack> recentTracks){
        List<Integer> popularity = recentTracks
                .stream()
                .map(UserRecentTrack::getPopularity)
                .toList();
        return mathUtils.getAverage(popularity);
    }

    public double calculateMainStreamScore(double artistAvg, double topTrackAvg, double recentTrackAvg){
        double mainstreamScore = mathUtils.calculateWeightedAverageForMainstreamScore(artistAvg, topTrackAvg, recentTrackAvg);
        log.info("Mainstream score: {}", mainstreamScore);
        return mainstreamScore;
    }

    public double calculateDiscoveryRate(List<UserTopTrack> topTracks, List<UserRecentTrack> recentTracks){
        Set<String> topTrackArtistIds = topTracks
                .stream()
                .flatMap(t -> t.getArtistIds().stream())
                .collect(Collectors.toSet());

        Set<String> recentTrackArtistIds = recentTracks
                .stream()
                .flatMap(r -> r.getArtistIds().stream())
                .collect(Collectors.toSet());

        double discoveryRate = mathUtils.calculateJaccardSimilarity(topTrackArtistIds, recentTrackArtistIds);
        log.info("Discovery rate: {}", discoveryRate);

        return discoveryRate;
    }

}
