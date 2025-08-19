package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.math.MathUtils;
import com.victor.VibeMatch.userartist.UserArtist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompatibilityCalculationServiceImplTest {

    @Mock
    private MathUtils mathUtils;

    @InjectMocks
    private CompatibilityCalculationServiceImpl compatibilityCalculationService;

    private Map<String, UserArtist> artists1Map;
    private Map<String, UserArtist> artists2Map;

    @BeforeEach
    public void setUp() {
        // Create the initial lists
        List<UserArtist> artists1 = List.of(
                UserArtist.builder()
                        .artistSpotifyId("7hK1Q5q4bWqg9r0947g8j7")
                        .name("Drake")
                        .popularity(100)
                        .ranking(1)
                        .genres(Set.of("hip hop", "rap"))
                        .build(),
                UserArtist.builder()
                        .artistSpotifyId("2lUqiBv51pYq3yLq8f9d02")
                        .name("Kendrick Lamar")
                        .popularity(95)
                        .ranking(2)
                        .genres(Set.of("hip hop", "rap"))
                        .build(),
                UserArtist.builder()
                        .artistSpotifyId("3fM6U9c8F2v7a88s1b12b3")
                        .name("J. Cole")
                        .popularity(90)
                        .ranking(3)
                        .genres(Set.of("hip hop", "rap"))
                        .build(),
                UserArtist.builder()
                        .artistSpotifyId("6lUqiBv51pYq3yLq8f9d02")
                        .name("Eminem")
                        .popularity(98)
                        .ranking(4)
                        .genres(Set.of("hip hop", "rap"))
                        .build()
        );

        List<UserArtist> artists2 = List.of(
                UserArtist.builder()
                        .artistSpotifyId("4rUqiBv51pYq3yLq8f9d02")
                        .name("Post Malone")
                        .popularity(94)
                        .ranking(1)
                        .genres(Set.of("hip hop", "pop"))
                        .build(),
                UserArtist.builder()
                        .artistSpotifyId("6lUqiBv51pYq3yLq8f9d02")
                        .name("Eminem")
                        .popularity(98)
                        .ranking(5)
                        .genres(Set.of("hip hop", "rap"))
                        .build(),
                UserArtist.builder()
                        .artistSpotifyId("9oIqiBv51pYq3yLq8f9d02")
                        .name("Lil Wayne")
                        .popularity(88)
                        .ranking(3)
                        .genres(Set.of("hip hop", "rap"))
                        .build(),
                UserArtist.builder()
                        .artistSpotifyId("2iSqiBv51pYq3yLq8f9d02")
                        .name("Taylor Swift")
                        .popularity(99)
                        .ranking(4)
                        .genres(Set.of("pop", "country"))
                        .build()
        );

        // Convert the lists to maps for the new test
        artists1Map = artists1.stream()
                .collect(Collectors.toMap(UserArtist::getArtistSpotifyId, a -> a));

        artists2Map = artists2.stream()
                .collect(Collectors.toMap(UserArtist::getArtistSpotifyId, a -> a));
    }


    @Test
    void getSharedArtists_givenTwoArtistMaps_shouldReturnSharedArtists() {
        //Arrange
        List<CompatibilityWrapper> expected = List.of(
                new CompatibilityWrapper("Eminem", 4, 5)
        );

        //Act
        List<CompatibilityWrapper> wrappers = compatibilityCalculationService.getSharedArtists(artists1Map, artists2Map);

        //Assert
        assertNotNull(wrappers, "The returned list of wrappers should not be null.");
        assertEquals(expected.size(), wrappers.size(), "The number of shared artists should match the expected size.");
        assertEquals(expected.getFirst().getName(), wrappers.getFirst().getName(), "The name of the shared artist should be Eminem.");
        assertEquals(expected.getFirst().getYour(), wrappers.getFirst().getYour(), "Your ranking should be 4.");
        assertEquals(expected.getFirst().getTheir(), wrappers.getFirst().getTheir(), "Their ranking should be 5.");
    }

    @Test
    void getSharedGenres_givenTwoGenreLists_shouldReturnSharedGenres() {
        // Arrange
        List<String> genres1 = List.of("hip hop", "rap", "pop", "rnb", "hip hop");
        List<String> genres2 = List.of("pop", "rock", "hip hop", "rnb", "country");

        Map<String, Integer> genres1Map = new HashMap<>();
        genres1Map.put("hip hop", 2);
        genres1Map.put("rap", 1);
        genres1Map.put("pop", 1);
        genres1Map.put("rnb", 1);

        Map<String, Integer> genres2Map = new HashMap<>();
        genres2Map.put("pop", 1);
        genres2Map.put("rock", 1);
        genres2Map.put("hip hop", 1);
        genres2Map.put("rnb", 1);
        genres2Map.put("country", 1);

        when(mathUtils.mapCountToKey(genres1)).thenReturn(genres1Map);
        when(mathUtils.mapCountToKey(genres2)).thenReturn(genres2Map);


        List<CompatibilityWrapper> expectedWrappers = List.of(
                new CompatibilityWrapper("hip hop", 2.0 / 5.0, 1.0 / 5.0), // hip hop appears twice in genres1
                new CompatibilityWrapper("pop", 1.0 / 5.0, 1.0 / 5.0),
                new CompatibilityWrapper("rnb", 1.0 / 5.0, 1.0 / 5.0)
        );

        // Act
        List<CompatibilityWrapper> actualWrappers = compatibilityCalculationService.getSharedGenres(genres1, genres2);

        // Assert
        assertNotNull(actualWrappers);
        assertEquals(expectedWrappers.size(), actualWrappers.size(), "Should return 3 shared genres");


        assertEquals(expectedWrappers.getFirst().getName(), actualWrappers.getFirst().getName());
        assertEquals(expectedWrappers.getFirst().getYour(), actualWrappers.getFirst().getYour(), 0.001);
        assertEquals(expectedWrappers.getFirst().getTheir(), actualWrappers.getFirst().getTheir(), 0.001);

        assertEquals(expectedWrappers.get(1).getName(), actualWrappers.get(1).getName());
        assertEquals(expectedWrappers.get(1).getYour(), actualWrappers.get(1).getYour(), 0.001);
        assertEquals(expectedWrappers.get(1).getTheir(), actualWrappers.get(1).getTheir(), 0.001);

        assertEquals(expectedWrappers.get(2).getName(), actualWrappers.get(2).getName());
        assertEquals(expectedWrappers.get(2).getYour(), actualWrappers.get(2).getYour(), 0.001);
        assertEquals(expectedWrappers.get(2).getTheir(), actualWrappers.get(2).getTheir(), 0.001);
    }


    @Test
    void calculateDiscoveryCompatibility_givenTwoScores_shouldReturnCorrectCompatibility() {
        // Arrange
        double discoveryScore1 = 0.8;
        double discoveryScore2 = 0.2;
        double mainstreamScore1 = 0.5;
        double mainstreamScore2 = 0.9;
        double expectedCompatibility = 0.46; // (1 - |0.8 - 0.2|) * 0.7 + (1 - |0.5 - 0.9|) * 0.3 = 0.4 * 0.7 + 0.6 * 0.3 = 0.28 + 0.18 = 0.46

        // Act
        double actualCompatibility = compatibilityCalculationService.calculateDiscoveryCompatibility(
                discoveryScore1, discoveryScore2, mainstreamScore1, mainstreamScore2
        );

        // Assert
        assertEquals(expectedCompatibility, actualCompatibility, 0.001);
    }

    @Test
    void calculateDiscoveryCompatibility_givenSimilarScores_shouldReturnHighScore() {
        // Arrange
        double discoveryScore1 = 0.8;
        double discoveryScore2 = 0.7;
        double mainstreamScore1 = 0.5;
        double mainstreamScore2 = 0.6;
        double expectedCompatibility = 0.9;

        // Act
        double actualCompatibility = compatibilityCalculationService.calculateDiscoveryCompatibility(
                discoveryScore1, discoveryScore2, mainstreamScore1, mainstreamScore2
        );

        // Assert
        assertEquals(expectedCompatibility, actualCompatibility, 0.001);
    }

    @Test
    void calculateTasteCompatibility_givenTwoArtistLists_shouldReturnCorrectCompatibility() {
        // Arrange
        List<UserArtist> artists1 = List.of(artists1Map.get("7hK1Q5q4bWqg9r0947g8j7"));
        List<UserArtist> artists2 = List.of(artists2Map.get("4rUqiBv51pYq3yLq8f9d02"));

        // Mock the dependencies
        when(mathUtils.calculateUserArtistWeightedVector(anyInt(), anyInt(), anyInt())).thenReturn(1.0);
        when(mathUtils.calculateCosineSimilarity(anyList(), anyList())).thenReturn(0.7);
        when(mathUtils.calculateJaccardSimilarity(anyList(), anyList())).thenReturn(0.5);

        double expectedCompatibility = (0.7 * 0.5) + (0.5 * 0.5); // (artistSimilarity * 0.5) + (genreOverlap * 0.5)
        double delta = 0.001;

        // Act
        double actualCompatibility = compatibilityCalculationService.calculateTasteCompatibility(
                artists1, artists2, artists1, artists2
        );

        // Assert
        assertEquals(expectedCompatibility, actualCompatibility, delta);
    }
}