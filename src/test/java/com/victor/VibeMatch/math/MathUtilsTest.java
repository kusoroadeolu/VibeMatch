package com.victor.VibeMatch.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MathUtilsTest {

    @InjectMocks
    private MathUtils mathUtils;

    @Test
    public void shouldCalculateJaccardSimilarity(){
        //Arrange
        List<String> list1 = List.of("apple", "grape", "orange");
        List<String> list2 = List.of("apple", "banana", "cherry", "grape", "kiwi");;
        double expected = 0.3333333333333333;

        //Act
        double similarity = mathUtils.calculateJaccardSimilarity(list1, list2);

        //Assert
        assertEquals(expected, similarity, 0.00000000000001);
    }

    @Test
    void testMapCountToKey_basicCase() {
        // Arrange
        List<String> inputList = List.of("apple", "banana", "apple", "cherry", "banana", "apple");
        HashMap<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("apple", 3);
        expectedMap.put("banana", 2);
        expectedMap.put("cherry", 1);



        // Act
        Map<String, Integer> actualMap = mathUtils.mapCountToKey(inputList);

        // Assert
        assertEquals(expectedMap, actualMap, "The map should correctly count occurrences of each string.");
    }

    @Test
    public void getAverage_shouldReturnAverageOfListOfNumbers(){
        //Arrange
        List<Integer> nums = List.of(1,2,3,4,5);
        double expected = 3.0d;

        //Act
        double avg = mathUtils.getAverage(nums);

        //Assert
        assertEquals(expected, avg);

    }

    @Test
    public void shouldCalculateWeightedAverageForMainstreamScore(){
        //Arrange
        double artistAvg = 0.6;
        double topTrackAvg = 0.3;
        double recentTrackAvg = 0.7;
        double expected = 0.53;

        //Act
        double mainstreamScore = mathUtils.calculateWeightedAverageForMainstreamScore(artistAvg, topTrackAvg, recentTrackAvg);

        assertEquals(expected, mainstreamScore);
    }

    @Test
    public void shouldCalculateUserArtistWeightedVector(){
        //Arrange
        int rank = 1;
        int maxRank = 25;
        int popularity = 45;
        double expected = 30.0d;

        //Act
        double weightedVector = mathUtils.calculateUserArtistWeightedVector(rank, maxRank, popularity);

        //Assert
        assertEquals(expected, weightedVector);
    }

    @Test
    public void shouldCalculateUserArtistWeightedVectorPopularitySkewed(){
        //Arrange
        int rank = 3;
        int maxRank = 25;
        int popularity = 70;
        double expected = 51.2d;

        //Act
        double weightedVector = mathUtils.calculateUserArtistWeightedVectorPopularitySkewed(rank, maxRank, popularity);

        //Assert
        assertEquals(expected, weightedVector);
    }

    @Test
    public void shouldInvertRanks(){
        //Arrange
        int rank = 1;
        int maxRank = 25;
        int expected = 25;

        //Act
        double inverted = mathUtils.invertRank(rank, maxRank);

        //Assert
        assertEquals(expected, inverted);

    }

    @Test
    public void shouldCalculateCosineSimilarity(){

        //Arrange
        List<Double> weights1 = List.of(19.99, 5.50, 42.00, 0.0);
        List<Double> weights2 = List.of(1.5, 2.3, 0.8, 1.1);
        double expectedSimilarity = 0.5311511590069463;

        //Act
        double cosineSimilarity = mathUtils.calculateCosineSimilarity(weights1, weights2);

        //Assert
        assertEquals(expectedSimilarity, cosineSimilarity);

    }
}