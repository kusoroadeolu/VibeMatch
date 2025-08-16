package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserArtistQueryServiceImplTest {

    @Mock
    private UserArtistRepository userArtistRepository;

    @InjectMocks
    private UserArtistQueryServiceImpl userArtistQueryService;

    // Test data
    private User testUser;
    private UserArtist userArtist1;
    private UserArtist userArtist2;
    private UserArtist userArtist3;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .build();

        userArtist1 = UserArtist.builder()
                .id(UUID.randomUUID())
                .name("Artist A")
                .artistSpotifyId("spotifyId1")
                .ranking(1)
                .popularity(80)
                .genres(new HashSet<>(Arrays.asList("Pop", "Rock")))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        userArtist2 = UserArtist.builder()
                .id(UUID.randomUUID())
                .name("Artist B")
                .artistSpotifyId("spotifyId2")
                .ranking(2)
                .popularity(70)
                .genres(new HashSet<>(List.of("Hip Hop")))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        userArtist3 = UserArtist.builder()
                .id(UUID.randomUUID())
                .name("Artist C")
                .artistSpotifyId("spotifyId3")
                .ranking(3)
                .popularity(60)
                .genres(new HashSet<>(List.of("Jazz")))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findArtistsByUser_shouldReturnAllArtistsForUser() {
        // Arrange: Prepare a list of artists that the repository should return
        List<UserArtist> expectedArtists = Arrays.asList(userArtist1, userArtist2);

        // Stub the repository method call
        // When userArtistRepository.findByUser is called with testUser, return expectedArtists
        when(userArtistRepository.findByUser(testUser)).thenReturn(expectedArtists);

        // Act: Call the service method
        List<UserArtist> actualArtists = userArtistQueryService.findArtistsByUser(testUser);

        // Assert: Verify the results
        assertNotNull(actualArtists, "The returned list of artists should not be null");
        assertEquals(expectedArtists.size(), actualArtists.size(), "The number of artists should match");
        assertEquals(expectedArtists, actualArtists, "The returned artists list should be identical to the expected list");

        // Verify that the repository method was called exactly once with the correct user
        verify(userArtistRepository, times(1)).findByUser(testUser);
        // Verify no other interactions with the mock
        verifyNoMoreInteractions(userArtistRepository);
    }

    @Test
    void findArtistsByUser_shouldReturnEmptyListIfNoArtistsFound() {
        // Arrange: Prepare an empty list for the repository to return
        List<UserArtist> expectedArtists = List.of(); // Or Collections.emptyList();

        // Stub the repository method call for an empty result
        when(userArtistRepository.findByUser(testUser)).thenReturn(expectedArtists);

        // Act: Call the service method
        List<UserArtist> actualArtists = userArtistQueryService.findArtistsByUser(testUser);

        // Assert: Verify the results for an empty list scenario
        assertNotNull(actualArtists, "The returned list of artists should not be null");
        assertEquals(0, actualArtists.size(), "The number of artists should be zero");
        assertEquals(expectedArtists, actualArtists, "The returned list should be empty");

        // Verify that the repository method was called exactly once
        verify(userArtistRepository, times(1)).findByUser(testUser);
        verifyNoMoreInteractions(userArtistRepository);
    }

    @Test
    void findArtistsByUserOrderByRanking_shouldReturnTopArtistsByLimit() {
        // Arrange
        int limit = 2;
        List<UserArtist> expectedArtists = Arrays.asList(userArtist1, userArtist2); // Assuming these are the top 2 by ranking

        // Create a Limit object matching the expected argument
        Limit limitArgument = Limit.of(limit);

        when(userArtistRepository.findByUserOrderByRankingAsc(testUser, limitArgument)).thenReturn(expectedArtists);

        // Act
        List<UserArtist> actualArtists = userArtistQueryService.findArtistsByUserOrderByRanking(testUser, limit);

        // Assert
        assertNotNull(actualArtists, "The returned list of artists should not be null");
        assertEquals(expectedArtists.size(), actualArtists.size(), "The number of artists should match the limit");
        assertEquals(expectedArtists, actualArtists, "The returned artists should be the top ranked ones");

        verify(userArtistRepository, times(1)).findByUserOrderByRankingAsc(testUser, limitArgument);
        verifyNoMoreInteractions(userArtistRepository);
    }

    @Test
    void findArtistsByUserOrderByRanking_shouldReturnFewerArtistsIfLessThanLimitAvailable() {
        // Arrange
        int limit = 5; // Requesting 5 artists
        List<UserArtist> expectedArtists = Arrays.asList(userArtist1, userArtist2, userArtist3); // Only 3 are available

        Limit limitArgument = Limit.of(limit);

        when(userArtistRepository.findByUserOrderByRankingAsc(testUser, limitArgument)).thenReturn(expectedArtists);

        // Act
        List<UserArtist> actualArtists = userArtistQueryService.findArtistsByUserOrderByRanking(testUser, limit);

        // Assert: Verify that it returns all available artists, even if fewer than the limit
        assertNotNull(actualArtists, "The returned list of artists should not be null");
        assertEquals(expectedArtists.size(), actualArtists.size(), "The number of artists should match available artists");
        assertEquals(expectedArtists, actualArtists, "The returned artists should be all available ranked artists");

        // Verify the interaction
        verify(userArtistRepository, times(1)).findByUserOrderByRankingAsc(testUser, limitArgument);
        verifyNoMoreInteractions(userArtistRepository);
    }

    @Test
    void findArtistsByUserOrderByRanking_shouldReturnEmptyListIfNoArtistsFoundForLimit() {
        // Arrange
        int limit = 3;
        List<UserArtist> expectedArtists = Arrays.asList();

        Limit limitArgument = Limit.of(limit);

        when(userArtistRepository.findByUserOrderByRankingAsc(testUser, limitArgument)).thenReturn(expectedArtists);

        // Act: Call the service method
        List<UserArtist> actualArtists = userArtistQueryService.findArtistsByUserOrderByRanking(testUser, limit);

        // Assert: Verify an empty list is returned
        assertNotNull(actualArtists, "The returned list of artists should not be null");
        assertEquals(0, actualArtists.size(), "The number of artists should be zero");
        assertEquals(expectedArtists, actualArtists, "The returned list should be empty");

        // Verify the interaction
        verify(userArtistRepository, times(1)).findByUserOrderByRankingAsc(testUser, limitArgument);
        verifyNoMoreInteractions(userArtistRepository);
    }
}
