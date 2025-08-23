package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.math.MathUtils;
import com.victor.VibeMatch.userartist.UserArtist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityCalculationServiceImpl implements com.victor.VibeMatch.compatibility.CompatibilityCalculationService {

    private final MathUtils mathUtils;

    //Gets shared artists between users
    @Override
    public List<CompatibilityWrapper> getSharedArtists(Map<String, UserArtist> map1, Map<String, UserArtist> map2){
        List<CompatibilityWrapper> sharedArtists = new LinkedList<>();
        int count = 0;

        for(Map.Entry<String, UserArtist> entry: map1.entrySet()){
            UserArtist artist = entry.getValue();
            String id = artist.getArtistSpotifyId();

            if(map2.containsKey(id) && count < 3){
                UserArtist theirArtist = map2.get(id);
                sharedArtists.add(
                        new CompatibilityWrapper(artist.getName(), artist.getRanking(), theirArtist.getRanking())
                );
                count++;
            }
        }

        log.info("Found {} shared artists between both users", sharedArtists.size());
        return sharedArtists;
    }

    //Gets shared genres
    @Override
    public List<CompatibilityWrapper> getSharedGenres(List<String> genres1, List<String> genres2){

        int genres1Size = genres1.size();
        int genres2Size = genres2.size();

        Map<String, Integer> genres1Map = mathUtils.mapCountToKey(genres1);
        Map<String, Integer> genres2Map = mathUtils.mapCountToKey(genres2);

        List<CompatibilityWrapper> sharedGenres = new LinkedList<>();
        int count = 0;

        for(String genre : genres1){
            if(genres2.contains(genre) && count < 3){
                double yourPercentage = (double) genres1Map.get(genre) /genres1Size;
                double theirPercentage = (double) genres2Map.get(genre) /genres2Size;

                sharedGenres.add(
                        new CompatibilityWrapper(genre, yourPercentage , theirPercentage)
                );
                count++;
            }
        }

        log.info("Found {} shared genres between both users", sharedGenres.size());
        return sharedGenres;
    }


    @Override
    public double calculateDiscoveryCompatibility(double discoveryScore1, double discoveryScore2, double mainstreamScore1, double mainstreamScore2){
        double discoveryDistance = Math.abs(discoveryScore1 - discoveryScore2);
        double mainstreamDistance = Math.abs(mainstreamScore1 - mainstreamScore2);
        log.info("Discovery distance: {}", discoveryDistance);
        log.info("Mainstream distance: {}", mainstreamDistance);

        double discoverySimilarity = 1 - discoveryDistance;
        double mainstreamSimilarity = 1 - mainstreamDistance;
        log.info("Discovery similarity: {}", discoveryDistance);
        log.info("Mainstream similarity: {}", mainstreamDistance);

        double compatibility = (0.7*discoverySimilarity)+(0.3*mainstreamSimilarity);
        log.info("Discover compatibility: {}", compatibility);
        return compatibility;
    }

    @Override
    public double calculateTasteCompatibility(List<UserArtist> artists1, List<UserArtist> artists2,
                                              List<UserArtist> filteredArtists1, List<UserArtist> filteredArtists2){
        int artist1Size = artists1.size();
        int artist2Size = artists2.size();
        int threshold = 65;

        //Calculate the avg pop of the first artists
        double artists1AvgPop = (double) artists1
                .stream()
                .map(UserArtist::getPopularity)
                .reduce(0, Integer::sum) / artist1Size;

        //Calculate the avg pop of the second artist list
        double artists2AvgPop = (double) artists1
                .stream()
                .map(UserArtist::getPopularity)
                .reduce(0, Integer::sum) / artist2Size;

        List<Double> weights1 = getWeightedVectorsBasedOnPopularity
                (artists1AvgPop, threshold, artist1Size, filteredArtists1);
        List<Double> weights2 = getWeightedVectorsBasedOnPopularity
                (artists2AvgPop, threshold, artist2Size, filteredArtists2);


        List<String> genres1 = artists1
                .stream()
                .flatMap(a -> a.getGenres().stream())
                .toList();
        List<String> genres2 = artists2
                .stream()
                .flatMap(a -> a.getGenres().stream())
                .toList();


        double artistSimilarity = mathUtils.calculateCosineSimilarity(weights1, weights2);
        log.info("Artist similarity: {}", artistSimilarity);

        double genreOverlap = mathUtils.calculateJaccardSimilarity(genres1, genres2);
        log.info("Genre similarity: {}", genreOverlap);

        return (artistSimilarity + genreOverlap) * .5;
    }




    public List<Double> getWeightedVectorsBasedOnPopularity(double avgPopularity, int threshold, int artistSize ,List<UserArtist> artists){
        List<Double> weights;
        if(avgPopularity >= threshold){
            weights = artists
                    .stream()
                    .map(artist -> mathUtils.calculateUserArtistWeightedVectorPopularitySkewed(artist.getRanking(), artistSize, artist.getPopularity()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }else{
            weights = artists
                    .stream()
                    .map(artist -> mathUtils.calculateUserArtistWeightedVector(artist.getRanking(), artistSize, artist.getPopularity()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return weights;
    }





}
