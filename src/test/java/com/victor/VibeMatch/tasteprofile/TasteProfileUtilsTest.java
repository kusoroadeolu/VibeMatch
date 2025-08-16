package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.utils.MathUtils;
import com.victor.VibeMatch.tasteprofile.utils.TasteProfileUtils;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TasteProfileUtilsTest {

    @Mock
    private MathUtils mathUtils;

    @InjectMocks
    private TasteProfileUtils tasteProfileUtils;

    private List<UserArtist> userArtists;

    @BeforeEach
    void setUp() {
        // Create sample UserArtist data for tests
        userArtists = new ArrayList<>();
        UserArtist artist1 = UserArtist.builder().genres(Set.of("pop", "rock")).popularity(80).build();
        UserArtist artist2 = UserArtist.builder().genres(Set.of("pop", "hip hop")).popularity(90).build();
        UserArtist artist3 = UserArtist.builder().genres(Set.of("hip hop")).popularity(70).build();
        UserArtist artist4 = UserArtist.builder().genres(Set.of("pop")).popularity(85).build();
        UserArtist artist5 = UserArtist.builder().genres(Set.of("rock", "indie")).popularity(75).build();

        userArtists.add(artist1);
        userArtists.add(artist2);
        userArtists.add(artist3);
        userArtists.add(artist4);
        userArtists.add(artist5);
    }

    @Test
    void testGetAllGenres_shouldReturnAllGenresFromAllArtists() {
        // Arrange - Data is set up in @BeforeEach

        // Act
        List<String> allGenres = tasteProfileUtils.getAllGenres(userArtists);

        // Assert
        assertEquals(8, allGenres.size(), "The total number of genres should be 8");
        assertEquals(3, Collections.frequency(allGenres, "pop"), "Pop should appear 3 times");
        assertEquals(2, Collections.frequency(allGenres, "rock"), "Rock should appear 2 times");
        assertEquals(2, Collections.frequency(allGenres, "hip hop"), "Hip hop should appear 2 times");
        assertEquals(1, Collections.frequency(allGenres, "indie"), "Indie should appear 1 time");
    }

    @Test
    void testGetAllGenres_shouldReturnEmptyListForEmptyInput() {
        // Arrange
        List<UserArtist> emptyArtists = new ArrayList<>();

        // Act
        List<String> allGenres = tasteProfileUtils.getAllGenres(emptyArtists);

        // Assert
        assertEquals(0, allGenres.size(), "The list of genres should be empty for an empty input");
    }

    @Test
    void testMapCountToTopString_shouldReturnTopThreeCountsCorrectly() {
        // Arrange
        Map<String, Integer> inputMap = new HashMap<>();
        inputMap.put("pop", 4);
        inputMap.put("rock", 3);
        inputMap.put("hip hop", 2);
        inputMap.put("jazz", 1);
        inputMap.put("classical", 1);

        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("pop", 4);
        expectedMap.put("rock", 3);
        expectedMap.put("hip hop", 2);

        // Act
        Map<String, Integer> actualMap = tasteProfileUtils.mapCountToTopString(inputMap);

        // Assert
        assertEquals(expectedMap.size(), actualMap.size(), "The resulting map should contain exactly 3 genres");
        assertEquals(expectedMap, actualMap, "The map should contain the top three genres by count");
    }

    @Test
    void testMapCountToTopString_shouldHandleTiesCorrectly() {
        // Arrange
        Map<String, Integer> inputMap = new HashMap<>();
        inputMap.put("pop", 4);
        inputMap.put("rock", 3);
        inputMap.put("hip hop", 3); // Hip hop is tied with rock
        inputMap.put("jazz", 2);

        Map<String, Integer> expectedMap = new LinkedHashMap<>();
        expectedMap.put("pop", 4);
        expectedMap.put("rock", 3);
        expectedMap.put("hip hop", 3);

        // Act
        Map<String, Integer> actualMap = tasteProfileUtils.mapCountToTopString(inputMap);

        // Assert
        assertEquals(expectedMap.size(), actualMap.size(), "The resulting map should have 3 genres");
        assertEquals(expectedMap, actualMap, "The map should contain the top genre, and then the next two unique counts.");
    }

    @Test
    void testGetTopGenres_shouldOrchestrateCorrectly() {
        // Arrange
        List<String> allGenres = tasteProfileUtils.getAllGenres(userArtists);
        Map<String, Integer> mockCountMap = new HashMap<>();
        mockCountMap.put("pop", 3);
        mockCountMap.put("rock", 2);
        mockCountMap.put("hip hop", 2);
        mockCountMap.put("indie", 1);

        Map<String, Integer> expectedTopGenres = new LinkedHashMap<>();
        expectedTopGenres.put("pop", 3);
        expectedTopGenres.put("rock", 2);
        expectedTopGenres.put("hip hop", 2);

        // Mock the dependency call to mathUtils.mapCountToKey
        when(mathUtils.mapCountToKey(allGenres)).thenReturn(mockCountMap);

        // Act
        Map<String, Integer> actualTopGenres = tasteProfileUtils.getTopGenres(userArtists);

        // Assert
        assertEquals(expectedTopGenres.size(), actualTopGenres.size(), "The resulting map should contain the top 3 genres");
        assertEquals(expectedTopGenres, actualTopGenres, "The service should correctly determine the top 3 genres");
    }

    @Test
    void testGetTopGenres_shouldHandleFewerThanThreeGenres() {
        // Arrange
        List<UserArtist> smallArtistList = new ArrayList<>();
        smallArtistList.add(UserArtist.builder().genres(Set.of("pop", "rock")).build());
        List<String> smallArtistGenres = List.of("pop", "rock");

        Map<String, Integer> mockCountMap = new HashMap<>();
        mockCountMap.put("pop", 1);
        mockCountMap.put("rock", 1);

        // Mock the dependency call
        when(mathUtils.mapCountToKey(smallArtistGenres)).thenReturn(mockCountMap);

        Map<String, Integer> expectedTopGenres = new LinkedHashMap<>();
        expectedTopGenres.put("pop", 1);
        expectedTopGenres.put("rock", 1);

        // Act
        Map<String, Integer> actualTopGenres = tasteProfileUtils.getTopGenres(smallArtistList);

        // Assert
        assertEquals(2, actualTopGenres.size(), "The map should contain both genres");
        assertEquals(expectedTopGenres, actualTopGenres, "The service should return all genres if count is less than 3");
    }

    @Test
    void testCalculateArtistPopularityAvg_shouldCalculateAveragePopularityCorrectly() {
        // Arrange
        List<Integer> popularities = userArtists.stream().map(UserArtist::getPopularity).collect(Collectors.toList());
        double expectedAvg = 80.0;
        when(mathUtils.getAverage(popularities)).thenReturn(expectedAvg);

        // Act
        double actualAvg = tasteProfileUtils.calculateArtistPopularityAvg(userArtists);

        // Assert
        assertEquals(expectedAvg, actualAvg, 0.01, "The average popularity should be calculated correctly");
    }

    @Test
    void testCalculateArtistPopularityAvg_shouldReturnZeroForEmptyList() {
        // Arrange
        List<UserArtist> emptyList = new ArrayList<>();
        when(mathUtils.getAverage(Collections.emptyList())).thenReturn(0.0);

        // Act
        double actualAvg = tasteProfileUtils.calculateArtistPopularityAvg(emptyList);

        // Assert
        assertEquals(0.0, actualAvg, 0.01, "Average of an empty list should be 0");
    }

    @Test
    void testCalculateTopTrackPopularityAvg_shouldCalculateAveragePopularityCorrectly() {
        // Arrange
        List<UserTopTrack> topTracks = new ArrayList<>();
        topTracks.add(UserTopTrack.builder().popularity(95).build());
        topTracks.add(UserTopTrack.builder().popularity(85).build());
        topTracks.add(UserTopTrack.builder().popularity(75).build());

        List<Integer> popularities = List.of(95, 85, 75);
        double expectedAvg = 85.0;
        when(mathUtils.getAverage(popularities)).thenReturn(expectedAvg);

        // Act
        double actualAvg = tasteProfileUtils.calculateTopTrackPopularityAvg(topTracks);

        // Assert
        assertEquals(expectedAvg, actualAvg, 0.01, "The average popularity should be calculated correctly");
    }

    @Test
    void testCalculateRecentTrackPopularityAvg_shouldCalculateAveragePopularityCorrectly() {
        // Arrange
        List<UserRecentTrack> recentTracks = new ArrayList<>();
        recentTracks.add(UserRecentTrack.builder().popularity(60).build());
        recentTracks.add(UserRecentTrack.builder().popularity(50).build());
        recentTracks.add(UserRecentTrack.builder().popularity(40).build());

        List<Integer> popularities = List.of(60, 50, 40);
        double expectedAvg = 50.0;
        when(mathUtils.getAverage(popularities)).thenReturn(expectedAvg);

        // Act
        double actualAvg = tasteProfileUtils.calculateRecentTrackPopularityAvg(recentTracks);

        // Assert
        assertEquals(expectedAvg, actualAvg, 0.01, "The average popularity should be calculated correctly");
    }

    @Test
    void testCalculateTopTrackPopularityAvg_shouldReturnZeroForEmptyList() {
        // Arrange
        List<UserTopTrack> emptyList = new ArrayList<>();
        when(mathUtils.getAverage(Collections.emptyList())).thenReturn(0.0);

        // Act
        double actualAvg = tasteProfileUtils.calculateTopTrackPopularityAvg(emptyList);

        // Assert
        assertEquals(0.0, actualAvg, 0.01, "Average of an empty list should be 0");
    }

    @Test
    void testCalculateRecentTrackPopularityAvg_shouldReturnZeroForEmptyList() {
        // Arrange
        List<UserRecentTrack> emptyList = new ArrayList<>();
        when(mathUtils.getAverage(Collections.emptyList())).thenReturn(0.0);

        // Act
        double actualAvg = tasteProfileUtils.calculateRecentTrackPopularityAvg(emptyList);

        // Assert
        assertEquals(0.0, actualAvg, 0.01, "Average of an empty list should be 0");
    }

    @Test
    public void shouldCalculateWeightedAverageForMainstreamScore(){
        //Arrange
        double artistAvg = 0.6;
        double topTrackAvg = 0.3;
        double recentTrackAvg = 0.7;
        double expected = 0.53;
        when(mathUtils.calculateWeightedAverageForMainstreamScore(artistAvg, topTrackAvg, recentTrackAvg)).thenReturn(expected);

        //Act
        double mainstreamScore = tasteProfileUtils.calculateMainStreamScore(artistAvg, topTrackAvg, recentTrackAvg);

        //Assert
        assertEquals(expected, mainstreamScore);
    }

    @Test
    public void shouldCalculateDiscoveryRate() {
        // Arrange
        UserTopTrack topTrack1 = UserTopTrack.builder()
                .id(UUID.randomUUID())
                .artistIds(new HashSet<>(List.of("artistIdX", "artistIdY")))
                .build();

        UserTopTrack topTrack2 = UserTopTrack.builder()
                .id(UUID.randomUUID())
                .artistIds(new HashSet<>(List.of("artistIdY", "artistIdZ")))
                .build();

        List<UserTopTrack> topTracks = List.of(topTrack1, topTrack2);

        UserRecentTrack recentTrack1 = UserRecentTrack.builder()
                .id(UUID.randomUUID())
                .name("Recent Song C")
                .artistIds(new HashSet<>(List.of("artistIdY", "artistIdW")))
                .build();

        UserRecentTrack recentTrack2 = UserRecentTrack.builder()
                .id(UUID.randomUUID())
                .artistIds(new HashSet<>(List.of("artistIdZ", "artistIdV")))
                .build();

        List<UserRecentTrack> recentTracks = List.of(recentTrack1, recentTrack2);

        double expectedDiscoveryRate = 0.5;
        when(mathUtils.calculateJaccardSimilarity(anySet(), anySet())).thenReturn(expectedDiscoveryRate);

        // Act

        double actualDiscoveryRate = tasteProfileUtils.calculateDiscoveryRate(topTracks, recentTracks);

        // Assert
        assertEquals(expectedDiscoveryRate, actualDiscoveryRate, 0.001); // Use a delta for double comparison
    }


}