package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityCalculationService;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompatibilityScoreCreationServiceImplTest {

    @Mock
    private CompatibilityCalculationService compatibilityCalculationService;

    @InjectMocks
    private CompatibilityScoreCreationServiceImpl compatibilityScoreCreationService;

    private User user;
    private User targetUser;
    private List<UserArtist> userArtists;
    private List<UserArtist> targetUserArtists;

    @BeforeEach
    void setUp() {
        // Setup mock users and their associated artists
        user = User.builder().username("user1").build();
        targetUser = User.builder().username("targetUser").build();

        userArtists = Arrays.asList(
                UserArtist.builder().artistSpotifyId("artist1").name("Artist A").genres(Set.of("pop", "rock")).ranking(1).build(),
                UserArtist.builder().artistSpotifyId("artist2").name("Artist B").genres(Set.of("hip hop", "rap")).ranking(2).build()
        );

        targetUserArtists = Arrays.asList(
                UserArtist.builder().artistSpotifyId("artist1").name("Artist A").genres(Set.of("pop", "rock")).ranking(3).build(),
                UserArtist.builder().artistSpotifyId("artist3").name("Artist C").genres(Set.of("classical")).ranking(1).build()
        );

        user.setUserArtists(userArtists);
        targetUser.setUserArtists(targetUserArtists);
    }

    @Test
    void getSharedArtists_should_return_correct_wrappers() {
        // Arrange
        List<CompatibilityWrapper> expectedWrappers = Collections.singletonList(
                CompatibilityWrapper.builder().name("Artist A").your(1).their(3).build()
        );
        when(compatibilityCalculationService.getSharedArtists(anyMap(), anyMap()))
                .thenReturn(expectedWrappers);

        // Act
        List<CompatibilityWrapper> result = compatibilityScoreCreationService.getSharedArtists(user, targetUser);

        // Assert
        assertEquals(expectedWrappers, result);
        verify(compatibilityCalculationService).getSharedArtists(anyMap(), anyMap());
    }

    @Test
    void getSharedGenres_should_return_correct_wrappers() {
        // Arrange
        List<CompatibilityWrapper> expectedWrappers = Collections.singletonList(
                CompatibilityWrapper.builder().name("pop").your(10).their(15).build()
        );
        when(compatibilityCalculationService.getSharedGenres(anyList(), anyList()))
                .thenReturn(expectedWrappers);

        // Act
        List<CompatibilityWrapper> result = compatibilityScoreCreationService.getSharedGenres(user, targetUser);

        // Assert
        assertEquals(expectedWrappers, result);
        verify(compatibilityCalculationService).getSharedGenres(anyList(), anyList());
    }

    @Test
    void getDiscoveryCompatibility_should_return_correct_score() {
        // Arrange
        double userDiscovery = 0.5;
        double targetDiscovery = 0.6;
        double userMainstream = 0.8;
        double targetMainstream = 0.7;
        double expectedScore = 0.95;

        user.setTasteProfile(TasteProfile.builder().discoveryPattern(userDiscovery).mainstreamScore(userMainstream).build());
        targetUser.setTasteProfile(TasteProfile.builder().discoveryPattern(targetDiscovery).mainstreamScore(targetMainstream).build());

        when(compatibilityCalculationService.calculateDiscoveryCompatibility(userDiscovery, targetDiscovery, userMainstream, targetMainstream))
                .thenReturn(expectedScore);

        // Act
        double result = compatibilityScoreCreationService.getDiscoveryCompatibility(user, targetUser);

        // Assert
        assertEquals(expectedScore, result);
        verify(compatibilityCalculationService).calculateDiscoveryCompatibility(userDiscovery, targetDiscovery, userMainstream, targetMainstream);
    }

    @Test
    void getTasteCompatibility_should_return_correct_score() {
        // Arrange
        double expectedScore = 0.85;
        int sharedArtists = 1;

        when(compatibilityCalculationService.calculateTasteCompatibility(
                anyList(), anyList(), anyList(), anyList(), sharedArtists
        )).thenReturn(expectedScore);

        // Act
        double result = compatibilityScoreCreationService.getTasteCompatibility(user, targetUser);

        // Assert
        assertEquals(expectedScore, result);
        verify(compatibilityCalculationService).calculateTasteCompatibility(
                anyList(), anyList(), anyList(), anyList(), anyInt()
        );
    }

    @Test
    void buildCompatibilityReasons_should_generate_correct_reasons() {
        // Test Case 1: All aspects present
        List<CompatibilityWrapper> sharedArtists = Arrays.asList(
                CompatibilityWrapper.builder().name("Artist X").your(5).their(10).build(),
                CompatibilityWrapper.builder().name("Artist Y").your(2).their(8).build()
        );
        List<CompatibilityWrapper> sharedGenres = Collections.singletonList(
                CompatibilityWrapper.builder().name("Indie Rock").your(0.2).their(0.25).build()
        );
        double discoveryCompatibilityHigh = 0.9;

        List<String> reasons1 = compatibilityScoreCreationService.buildCompatibilityReasons(sharedArtists, sharedGenres, discoveryCompatibilityHigh);
        assertEquals(4, reasons1.size());
        assertTrue(reasons1.contains("You have 2 artists in common."));
        assertTrue(reasons1.contains("You both enjoy Artist X."));
        assertTrue(reasons1.contains("You both love Indie Rock."));
        assertTrue(reasons1.contains("You have very similar music discovery patterns."));


        // Test Case 2: No shared artists, some shared genres, medium discovery
        List<CompatibilityWrapper> noSharedArtists = Collections.emptyList();
        List<CompatibilityWrapper> sharedGenres2 = Collections.singletonList(
                CompatibilityWrapper.builder().name("Electronic").your(0.15).their(0.18).build()
        );
        double discoveryCompatibilityMedium = 0.65;

        List<String> reasons2 = compatibilityScoreCreationService.buildCompatibilityReasons(noSharedArtists, sharedGenres2, discoveryCompatibilityMedium);
        assertEquals(2, reasons2.size());
        assertTrue(reasons2.contains("You both love Electronic."));
        assertTrue(reasons2.contains("You have similar music discovery patterns."));


        // Test Case 3: No shared genres, some shared artists, low discovery
        List<CompatibilityWrapper> sharedArtists3 = Collections.singletonList(
                CompatibilityWrapper.builder().name("Band Z").your(1).their(1).build()
        );
        List<CompatibilityWrapper> noSharedGenres = Collections.emptyList();
        double discoveryCompatibilityLow = 0.45;

        List<String> reasons3 = compatibilityScoreCreationService.buildCompatibilityReasons(sharedArtists3, noSharedGenres, discoveryCompatibilityLow);
        assertEquals(3, reasons3.size());
        assertTrue(reasons3.contains("You have 1 artists in common."));
        assertTrue(reasons3.contains("You both enjoy Band Z."));
        assertTrue(reasons3.contains("You have somewhat similar music discovery patterns."));


        // Test Case 4: Everything empty/low
        List<String> reasons4 = compatibilityScoreCreationService.buildCompatibilityReasons(Collections.emptyList(), Collections.emptyList(), 0.3);
        assertEquals(0, reasons4.size()); // No reasons generated if conditions aren't met
    }
}