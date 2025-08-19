package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapperUtils;
import com.victor.VibeMatch.tasteprofile.impl.TasteProfileCalculationServiceImpl;
import com.victor.VibeMatch.tasteprofile.utils.TasteProfileUtils;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.userartist.UserArtistQueryService;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrackQueryService;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrackQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasteProfileCalculationServiceImplTest {

    // Mock dependencies
    @Mock
    private UserTopTrackQueryService userTopTrackQueryService;
    @Mock
    private UserRecentTrackQueryService userRecentTrackQueryService;
    @Mock
    private TasteWrapperUtils tasteWrapperUtils;
    @Mock
    private UserArtistQueryService userArtistQueryService;
    @Mock
    private TasteProfileUtils tasteProfileUtils;

    // Inject mocks into the service being tested
    @InjectMocks
    private TasteProfileCalculationServiceImpl tasteProfileCalculationService;

    private User testUser;
    private List<UserArtist> userArtists;
    private List<UserRecentTrack> userRecentTracks;
    private List<UserTopTrack> userTopTracks;


    @BeforeEach
    void setUp() {

        // Create sample UserArtist data for tests
        userArtists = new ArrayList<>();
        UserArtist artist1 = UserArtist.builder().name("artist1").ranking(1).genres(Set.of("pop", "rock")).popularity(80).build();
        UserArtist artist2 = UserArtist.builder().name("artist2").ranking(2).genres(Set.of("pop", "hip hop")).popularity(90).build();
        UserArtist artist3 = UserArtist.builder().name("artist3").ranking(3).genres(Set.of("hip hop")).popularity(70).build();
        UserArtist artist4 = UserArtist.builder().genres(Set.of("pop")).popularity(85).build();
        UserArtist artist5 = UserArtist.builder().genres(Set.of("rock", "indie")).popularity(75).build();

        userArtists.add(artist1);
        userArtists.add(artist2);
        userArtists.add(artist3);
        userArtists.add(artist4);
        userArtists.add(artist5);

        // Create sample UserRecentTrack data
        userRecentTracks = new ArrayList<>();
        userRecentTracks.add(UserRecentTrack.builder()
                .name("Recent Track 1").artistNames(Set.of("Artist A")).artistIds(Set.of("idA"))
                .trackSpotifyId("track1").ranking(1).popularity(60).user(testUser)
                .createdAt(LocalDateTime.now()).build());
        userRecentTracks.add(UserRecentTrack.builder()
                .name("Recent Track 2").artistNames(Set.of("Artist B")).artistIds(Set.of("idB"))
                .trackSpotifyId("track2").ranking(2).popularity(55).user(testUser)
                .createdAt(LocalDateTime.now().minusHours(1)).build());
        userRecentTracks.add(UserRecentTrack.builder()
                .name("Recent Track 3").artistNames(Set.of("Artist C")).artistIds(Set.of("idC"))
                .trackSpotifyId("track3").ranking(3).popularity(70).user(testUser)
                .createdAt(LocalDateTime.now().minusHours(2)).build());
        userRecentTracks.add(UserRecentTrack.builder()
                .name("Recent Track 4").artistNames(Set.of("Artist D")).artistIds(Set.of("idD"))
                .trackSpotifyId("track4").ranking(4).popularity(65).user(testUser)
                .createdAt(LocalDateTime.now().minusHours(3)).build());
        userRecentTracks.add(UserRecentTrack.builder()
                .name("Recent Track 5").artistNames(Set.of("Artist E")).artistIds(Set.of("idE"))
                .trackSpotifyId("track5").ranking(5).popularity(80).user(testUser)
                .createdAt(LocalDateTime.now().minusHours(4)).build());


        // Create sample UserTopTrack data
        userTopTracks = new ArrayList<>();
        userTopTracks.add(UserTopTrack.builder()
                .name("Top Track 1").artistNames(Set.of("Artist X")).artistIds(Set.of("idx"))
                .trackSpotifyId("toptrack1").ranking(1).popularity(90).user(testUser).build());
        userTopTracks.add(UserTopTrack.builder()
                .name("Top Track 2").artistNames(Set.of("Artist Y")).artistIds(Set.of("idy"))
                .trackSpotifyId("toptrack2").ranking(2).popularity(85).user(testUser).build());
        userTopTracks.add(UserTopTrack.builder()
                .name("Top Track 3").artistNames(Set.of("Artist Z")).artistIds(Set.of("idz"))
                .trackSpotifyId("toptrack3").ranking(3).popularity(92).user(testUser).build());
        userTopTracks.add(UserTopTrack.builder()
                .name("Top Track 4").artistNames(Set.of("Artist P")).artistIds(Set.of("idp"))
                .trackSpotifyId("toptrack4").ranking(4).popularity(78).user(testUser).build());
        userTopTracks.add(UserTopTrack.builder()
                .name("Top Track 5").artistNames(Set.of("Artist Q")).artistIds(Set.of("idq"))
                .trackSpotifyId("toptrack5").ranking(5).popularity(88).user(testUser).build());

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .userArtists(userArtists)
                .userRecentTracks(userRecentTracks)
                .userTopTracks(userTopTracks)
                .build();
    }

    @Test
    void calculateTopGenres_shouldReturnCorrectTasteWrappersForValidUser() {
        // Arrange
        TasteWrapper tasteWrapper1 = TasteWrapper.builder().name("pop").percentage(37.5d).count(3).build();
        TasteWrapper tasteWrapper2 = TasteWrapper.builder().name("rock").percentage(25.0d).count(3).build();
        TasteWrapper tasteWrapper3 = TasteWrapper.builder().name("hip hop").percentage(25.0d).count(3).build();

        List<String> allGenresList = Arrays.asList("pop", "rock", "pop", "hip hop", "hip hop", "pop", "rock", "indie");
        when(tasteProfileUtils.getAllGenres(userArtists)).thenReturn(allGenresList);

        Map<String, Integer> topGenresMap = new HashMap<>();
        topGenresMap.put("pop", 3);
        topGenresMap.put("rock", 2);
        topGenresMap.put("hip hop", 2);
        when(tasteProfileUtils.getTopGenres(userArtists)).thenReturn(topGenresMap);


        when(tasteWrapperUtils.buildTasteWrapper("pop", 37.5d, 3)).thenReturn(tasteWrapper1);
        when(tasteWrapperUtils.buildTasteWrapper("rock", 25.0d, 2)).thenReturn(tasteWrapper2);
        when(tasteWrapperUtils.buildTasteWrapper("hip hop", 25.0d, 2)).thenReturn(tasteWrapper3);

        //Act
        List<TasteWrapper> tasteWrappers = tasteProfileCalculationService.calculateTopGenres(testUser);

        //Assert
        assertNotNull(tasteWrappers);
        assertEquals(tasteWrapper1, tasteWrappers.getFirst());
        assertEquals(tasteWrapper2, tasteWrappers.get(1));
        assertEquals(tasteWrapper3, tasteWrappers.get(2));
        verify(tasteWrapperUtils, times(3)).buildTasteWrapper(anyString(), anyDouble() ,anyInt());

    }

    @Test
    void calculateTopGenres_shouldHandleNoArtistsFound() {
        // Arrange
        User user = User.builder().username("username").build();

        // Act
        List<TasteWrapper> result = tasteProfileCalculationService.calculateTopGenres(testUser);

        // Assert
        verifyNoInteractions(tasteWrapperUtils);
        assertTrue(result.isEmpty(), "Result list should be empty if no artists are found.");
    }

    @Test
    void calculateTopGenres_shouldHandleFewerThanThreeTopGenres() {
        // Arrange
        List<UserArtist> singleArtistList = List.of(UserArtist.builder().genres(Set.of("jazz")).build());
        User user = User.builder().username("username").userArtists(singleArtistList).build();

        List<String> singleGenreList = List.of("jazz");
        when(tasteProfileUtils.getAllGenres(singleArtistList)).thenReturn(singleGenreList);

        Map<String, Integer> singleTopGenreMap = Map.of("jazz", 1);
        when(tasteProfileUtils.getTopGenres(singleArtistList)).thenReturn(singleTopGenreMap);

        when(tasteWrapperUtils.buildTasteWrapper("jazz", 100.0f, 1))
                .thenReturn(TasteWrapper.builder().name("jazz").percentage(100.0f).count(1).build());

        // Act
        List<TasteWrapper> result = tasteProfileCalculationService.calculateTopGenres(user);

        // Assert
        verify(tasteProfileUtils).getAllGenres(singleArtistList);
        verify(tasteProfileUtils).getTopGenres(singleArtistList);
        verify(tasteWrapperUtils, times(1)).buildTasteWrapper("jazz", 100.0f, 1);

        assertEquals(1, result.size(), "Should return 1 TasteWrapper object.");
        TasteWrapper jazzWrapper = result.getFirst();
        assertEquals("jazz", jazzWrapper.getName());
        assertEquals(1, jazzWrapper.getCount());
        assertEquals(100.0f, jazzWrapper.getPercentage(), 0.001f);
    }

    @Test
    public void calculateTopArtists_shouldReturnTopThreeArtists(){
        //Arrange
        User user = User.builder().username("username").build();
        List<UserArtist> artists = List.of(userArtists.getFirst(), userArtists.get(1), userArtists.get(2));
        when(userArtistQueryService.findArtistsByUserOrderByRanking(user, 3)).thenReturn(artists);

        TasteWrapper tasteWrapper1 = TasteWrapper.builder().name("artist1").ranking(1).build();
        TasteWrapper tasteWrapper2 = TasteWrapper.builder().name("artist2").ranking(2).build();
        TasteWrapper tasteWrapper3 = TasteWrapper.builder().name("artist3").ranking(3).build();

        when(tasteWrapperUtils.buildTasteWrapper("artist1", 1)).thenReturn(tasteWrapper1);
        when(tasteWrapperUtils.buildTasteWrapper("artist2", 2)).thenReturn(tasteWrapper2);
        when(tasteWrapperUtils.buildTasteWrapper("artist3", 3)).thenReturn(tasteWrapper3);

        //Act
        List<TasteWrapper> mockWrappers = tasteProfileCalculationService.calculateTopArtists(user);

        //Assert
        assertNotNull(mockWrappers);
        assertEquals(tasteWrapper1, mockWrappers.getFirst());
        assertEquals(tasteWrapper2, mockWrappers.get(1));
        assertEquals(tasteWrapper3, mockWrappers.get(2));
        verify(tasteWrapperUtils, times(3)).buildTasteWrapper(anyString(), anyInt());

    }

    @Test
    public void calculateTopArtists_shouldHandleEmptyList(){
        //Arrange
        List<UserArtist> artists = Collections.emptyList();
        when(userArtistQueryService.findArtistsByUserOrderByRanking(testUser, 3)).thenReturn(artists);

        //Act
        List<TasteWrapper> mockWrappers = tasteProfileCalculationService.calculateTopArtists(testUser);

        //Assert
        assertNotNull(mockWrappers);
        assertEquals(0, mockWrappers.size());
        verify(tasteWrapperUtils, never()).buildTasteWrapper(anyString(), anyInt());

    }

    @Test
    public void shouldCalculateMainstreamScore(){
        double expectedArtistAvg = 80.0d;
        double expectedTopTrackAvg = 66.0d;
        double expectedRecentTrackAvg = 86.6d;
        double expectedMainstreamScore = 0.7712d;

        when(tasteProfileUtils.calculateArtistPopularityAvg(userArtists)).thenReturn(expectedArtistAvg);
        when(tasteProfileUtils.calculateTopTrackPopularityAvg(userTopTracks)).thenReturn(expectedTopTrackAvg);
        when(tasteProfileUtils.calculateRecentTrackPopularityAvg(userRecentTracks)).thenReturn(expectedRecentTrackAvg);
        when(tasteProfileUtils.calculateMainStreamScore(expectedArtistAvg/100, expectedTopTrackAvg/100, expectedRecentTrackAvg/100))
                .thenReturn(expectedMainstreamScore);


        //Act
        double mainstreamScore = tasteProfileCalculationService.calculateMainStreamScore(testUser);

        //Assert
        assertEquals(expectedMainstreamScore, mainstreamScore);
    }

    @Test
    public void shouldCalculateDiscoveryPattern(){
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
            User user = User.builder().username("username").userTopTracks(topTracks).userRecentTracks(recentTracks).build();

            double expectedDiscoveryRate = 0.5;
            when(tasteProfileUtils.calculateDiscoveryRate(topTracks, recentTracks)).thenReturn(expectedDiscoveryRate);

            // Act
            double actualDiscoveryRate = tasteProfileCalculationService.calculateDiscoveryPattern(user);

            // Assert
            assertEquals(expectedDiscoveryRate, actualDiscoveryRate, 0.001);
        }
}
