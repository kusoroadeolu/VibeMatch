package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.utils.MathUtils;
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

}