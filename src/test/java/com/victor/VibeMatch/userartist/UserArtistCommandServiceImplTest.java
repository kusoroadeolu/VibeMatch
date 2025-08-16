package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.exceptions.UserArtistSaveException;
import com.victor.VibeMatch.user.User; // Assuming User class is accessible
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet; // Use HashSet for Set<String>
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserArtistCommandService Unit Tests")
public class UserArtistCommandServiceImplTest {

    @InjectMocks
    private UserArtistCommandServiceImpl userArtistCommandService;

    @Mock
    private UserArtistRepository userArtistRepository;



    // Helper method to create a UserArtist using the Builder pattern
    // This simplifies test data creation and adapts to changes in the UserArtist constructor/fields
    private UserArtist createUserArtist(String artistSpotifyId, String name, int popularity, Set<String> genres, int ranking, User user) {
        return UserArtist.builder()
                .id(UUID.randomUUID()) // Generate a random UUID for the test object
                .artistSpotifyId(artistSpotifyId)
                .name(name)
                .popularity(popularity)
                .genres(genres)
                .ranking(ranking)
                .user(user)
                .createdAt(LocalDateTime.now()) // Set current time for test object to ensure full object is built
                .build();
    }

    // --- Smart Test Case 1: Successful Save ---
    @Test
    @DisplayName("Should successfully save a list of UserArtists")
    void saveUserArtists_success() {
        // Given: A list of UserArtist objects to be saved
        User user1 = User.builder().username("user1").build();
        UserArtist userArtist1 = createUserArtist("artistId1", "Artist One", 70, new HashSet<>(Arrays.asList("pop", "rock")), 1, user1);
        UserArtist userArtist2 = createUserArtist("artistId2", "Artist Two", 60, new HashSet<>(Collections.singletonList("jazz")), 2, user1);
        List<UserArtist> userArtistsToSave = Arrays.asList(userArtist1, userArtist2);

        // When: The repository's saveAll method is called, it returns the same list
        when(userArtistRepository.saveAll(userArtistsToSave)).thenReturn(userArtistsToSave);

        // Then: The service should return the saved list
        List<UserArtist> savedUserArtists = userArtistCommandService.saveUserArtists(userArtistsToSave);

        // Verify: The saveAll method was invoked exactly once with the correct arguments
        verify(userArtistRepository, times(1)).saveAll(userArtistsToSave);
        // Assert: The returned list is not null and has the expected size and content
        assertNotNull(savedUserArtists);
        assertEquals(userArtistsToSave.size(), savedUserArtists.size());
        assertEquals(userArtistsToSave, savedUserArtists); // Relies on UserArtist having proper equals/hashCode
    }

    @Test
    @DisplayName("Should throw UserArtistSaveException on DataIntegrityViolationException")
    void saveUserArtists_handlesDataIntegrityViolationException() {
        // Given: A scenario where saving causes a data integrity issue
        User user1 = User.builder().username("user1").build();
        List<UserArtist> userArtistsToSave = Collections.singletonList(
                createUserArtist("artistId1", "Artist One", 70, new HashSet<>(Arrays.asList("pop")), 1, user1)
        );

        // When: The repository's saveAll method throws DataIntegrityViolationException
        doThrow(new DataIntegrityViolationException("Duplicate entry for artistId"))
                .when(userArtistRepository).saveAll(userArtistsToSave);

        // Then: The service should re-throw a UserArtistSaveException
        UserArtistSaveException thrownException = assertThrows(UserArtistSaveException.class, () ->
                userArtistCommandService.saveUserArtists(userArtistsToSave)
        );

        // Verify: The saveAll method was invoked exactly once
        verify(userArtistRepository, times(1)).saveAll(userArtistsToSave);
        // Assert: The exception message indicates a data integrity error
        assertTrue(thrownException.getMessage().contains("data integrity error occurred"));
        assertTrue(thrownException.getMessage().contains("Size: 1"));
    }

    @Test
    @DisplayName("Should throw UserArtistSaveException on any other generic Exception")
    void saveUserArtists_handlesGenericException() {
        // Given: A scenario where saving causes an unexpected runtime error
        User user1 = User.builder().username("user1").build();

        List<UserArtist> userArtistsToSave = Collections.singletonList(
                createUserArtist("artistId1", "Artist One", 70, new HashSet<>(Arrays.asList("pop")), 1, user1)
        );

        // When: The repository's saveAll method throws a generic RuntimeException
        doThrow(new RuntimeException("Database connection lost"))
                .when(userArtistRepository).saveAll(userArtistsToSave);

        // Then: The service should re-throw a UserArtistSaveException
        UserArtistSaveException thrownException = assertThrows(UserArtistSaveException.class, () ->
                userArtistCommandService.saveUserArtists(userArtistsToSave)
        );

        // Verify: The saveAll method was invoked exactly once
        verify(userArtistRepository, times(1)).saveAll(userArtistsToSave);
        // Assert: The exception message indicates an unexpected error
        assertTrue(thrownException.getMessage().contains("unexpected error occurred"));
        assertTrue(thrownException.getMessage().contains("Size: 1"));
    }

    @Test
    @DisplayName("Should return an empty list when saving an empty list of UserArtists")
    void saveUserArtists_emptyList() {
        // Given: An empty list of UserArtist objects
        List<UserArtist> emptyList = Collections.emptyList();

        // When: The repository's saveAll method is called with an empty list, it returns an empty list
        when(userArtistRepository.saveAll(emptyList)).thenReturn(emptyList);

        // Then: The service should return an empty list
        List<UserArtist> savedUserArtists = userArtistCommandService.saveUserArtists(emptyList);

        // Verify: The saveAll method was still invoked once (even if with an empty list)
        verify(userArtistRepository, times(1)).saveAll(emptyList);
        // Assert: The returned list is empty
        assertTrue(savedUserArtists.isEmpty());
    }
}
