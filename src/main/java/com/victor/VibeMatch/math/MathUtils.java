package com.victor.VibeMatch.math;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Slf4j
public class MathUtils{

    //Calculates the similarity between two collections of string
    public double calculateJaccardSimilarity(Collection<? extends String> a, Collection<? extends String> b){
        Set<String> aSet = new HashSet<>(a);
        Set<String> bSet = new HashSet<>(b);

        Set<String> intersection = new HashSet<>(aSet);
        intersection.retainAll(bSet);
        int overlap = intersection.size();

        Set<String> union = new HashSet<>(aSet);
        union.addAll(bSet);
        int unique = union.size();

        if(unique == 0){
            return 0.0;
        }

        return (double) overlap/unique;

    }

    public double calculateCosineSimilarity(List<Double> weights1, List<Double> weights2){
        log.info("Attempting to calculate cosine similarity");
        double totalDotProduct = 0.0d;
        double totalWeight1Sqr = 0.0d;
        double totalWeight2Sqr = 0.0d;
        log.info("Weights 1 size: {}", weights1.size());
        log.info("Weights 2 size: {}", weights2.size());

        for(int i = 0; i < weights1.size(); i++){
            double weight1 = weights1.get(i);
            double weight2 = weights2.get(i);

            double dotProduct = weight1 * weight2;
            totalDotProduct += dotProduct;

            double weight1Sqr = weight1 * weight1;
            double weight2Sqr = weight2 * weight2;

            totalWeight1Sqr += weight1Sqr;
            totalWeight2Sqr += weight2Sqr;
        }

        double weight1Sqrt = Math.sqrt(totalWeight1Sqr);
        double weight2Sqrt = Math.sqrt(totalWeight2Sqr);

        log.info("Dot Product: {}", totalDotProduct);
        log.info("Weight 1 Square Root: {}", weight1Sqrt);
        log.info("Weight 2 Square Root: {}", weight2Sqrt);

        if (weight1Sqrt == 0.0d || weight2Sqrt == 0.0d) {
            log.warn("One of the vectors is a zero vector. Returning 0.0.");
            return 0.0d;
        }

        double cosineSimilarity = totalDotProduct / (weight1Sqrt * weight2Sqrt);
        log.info("Cosine similarity: {}", cosineSimilarity);
        return cosineSimilarity;
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


    public double calculateUserArtistWeightedVector(int rank, int maxRank, int popularity){
        double invertedRank = (double)invertRank(rank, maxRank)/maxRank;
        double normalizedPopularity = (double) popularity /100;
        return (0.75 * invertedRank) + (0.25 * normalizedPopularity);
    }

    public double calculateUserArtistWeightedVectorPopularitySkewed(int rank, int maxRank ,int popularity){
        double invertedRank = (double) invertRank(rank, maxRank) /maxRank;
        double normalizedPopularity = (double) popularity /100;
        return (0.4 * invertedRank) + (0.6 * normalizedPopularity);
    }

    //Normalize artist ranks to point in the direction of popularity
    public int invertRank(int rank, int maxRank){
        return maxRank - rank + 1;
    }

    public Double round(Double val){
        return new BigDecimal(val.toString()).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
