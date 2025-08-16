package com.victor.VibeMatch.tasteprofile.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MathUtils{

    //Calculates the similarity between two collections of string
    public double calculateJaccardSimilarity(Collection<? extends String> a, Collection<? extends String> b){
        int overlap = 0;
        int aSize = a.size();
        int bSize = b.size();

        List<String> newList = new ArrayList<>(a);

        log.info("A list size: {}", aSize);
        log.info("B list size: {}", bSize);

        for(int i = 0; i < aSize; i++){
            String val = newList.get(i);
            if(b.contains(val)){
                overlap++;
            }
        }
        log.info("Overlap items: {}", overlap);

        int unique = (aSize + bSize) - overlap;
        log.info("Unique items: {}", unique);

        double similarity = (double) overlap /unique;
        log.info("Jaccard similarity: {}", similarity);
        return similarity;
    }


    public Map<String, Integer> mapCountToKey(List<String> list){
        HashMap<String, Integer> map = new HashMap<>();

        for(String s: list){
            map.put(s, map.getOrDefault(s, 0) + 1);
        }

        return map;
    }

    public double getAverage(List<Integer> nums){
        int total = nums
                .stream()
                .reduce(0 , Integer::sum);
        return (double) total/nums.size();
    }

    public double calculateWeightedAverageForMainstreamScore(double artistAvg, double topTrackAvg, double recentTrackAvg){
        return (0.5 * artistAvg) + (0.3 * topTrackAvg) + (0.2 * recentTrackAvg);
    }




}
