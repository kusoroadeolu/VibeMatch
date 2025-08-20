package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.tasteprofile.impl.TasteProfilePersistenceServiceImpl;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasteProfilePersistenceServiceImplTest {

    @Mock
    private TasteProfileCalculationService tasteProfileCalculationService;

    @Mock
    private TasteProfileCommandService tasteProfileCommandService;

    @Mock
    private UserQueryService userQueryService;

    // Use a spy to test the internal methods like buildTasteProfile and updateExistingTasteProfile.
    @InjectMocks
    private TasteProfilePersistenceServiceImpl tasteProfilePersistenceService;

    private User user;
    private UUID mockId;

    @BeforeEach
    public void setUp() {
        mockId = UUID.randomUUID();
        // A user with a null taste profile to simulate a creation scenario.
        user = User.builder().id(mockId).username("mock-name").tasteProfile(null).build();
    }

    // New test case for the creation path.
    @Test
    void createUserTasteProfile_shouldReturnSavedProfileOnCreation() {
        // Arrange
        TasteProfile newProfile = TasteProfile.builder().id(UUID.randomUUID()).build();

        // Configure mock behavior. The user is set up in the @BeforeEach with a null taste profile.
        when(userQueryService.findByUserId(mockId)).thenReturn(user);
        when(tasteProfileCalculationService.calculateTopGenres(user)).thenReturn(List.of(new TasteWrapper("genre1", 0.5, 1, 1)));
        when(tasteProfileCalculationService.calculateTopArtists(user)).thenReturn(List.of(new TasteWrapper("artist1", 0.6, 1, 1)));
        when(tasteProfileCalculationService.calculateDiscoveryPattern(user)).thenReturn(0.7d);
        when(tasteProfileCalculationService.calculateMainStreamScore(user)).thenReturn(0.8d);
        when(tasteProfileCommandService.saveTasteProfile(any(TasteProfile.class))).thenReturn(newProfile);

        // Act
        TasteProfile savedProfile = tasteProfilePersistenceService.createUserTasteProfile(mockId);

        // Assert
        assertNotNull(savedProfile);
        assertEquals(newProfile, savedProfile);

        // Verify that the buildTasteProfile method was called internally.
        verify(tasteProfileCalculationService, times(1)).calculateTopGenres(user);
        verify(tasteProfileCalculationService, times(1)).calculateTopArtists(user);
        verify(tasteProfileCalculationService, times(1)).calculateDiscoveryPattern(user);
        verify(tasteProfileCalculationService, times(1)).calculateMainStreamScore(user);

        // Capture the TasteProfile object passed to the save method to assert its contents.
        ArgumentCaptor<TasteProfile> captor = ArgumentCaptor.forClass(TasteProfile.class);
        verify(tasteProfileCommandService, times(1)).saveTasteProfile(captor.capture());

        TasteProfile capturedProfile = captor.getValue();
        assertNotNull(capturedProfile.getUser());
        assertEquals(user.getId(), capturedProfile.getUser().getId());
    }

    // New test case for the update path.
    @Test
    void createUserTasteProfile_shouldUpdateExistingProfile() {
        // Arrange
        UUID tasteProfileId = UUID.randomUUID();
        TasteProfile existingProfile = TasteProfile.builder()
                .id(tasteProfileId)
                .user(user)
                .topGenres(List.of(new TasteWrapper("oldGenre", 0.2, 1, 1)))
                .build();
        user.setTasteProfile(existingProfile);

        // Mock the user query to return a user with an existing profile.
        when(userQueryService.findByUserId(mockId)).thenReturn(user);

        // Mock calculation services for the updated values.
        List<TasteWrapper> newGenres = List.of(new TasteWrapper("newGenre", 0.9, 1, 1));
        when(tasteProfileCalculationService.calculateTopGenres(user)).thenReturn(newGenres);
        when(tasteProfileCalculationService.calculateTopArtists(user)).thenReturn(List.of());
        when(tasteProfileCalculationService.calculateDiscoveryPattern(user)).thenReturn(0.9d);
        when(tasteProfileCalculationService.calculateMainStreamScore(user)).thenReturn(0.9d);

        // Mock the save command service to return the updated profile.
        when(tasteProfileCommandService.saveTasteProfile(any(TasteProfile.class))).thenReturn(existingProfile);

        // Act
        TasteProfile updatedProfile = tasteProfilePersistenceService.createUserTasteProfile(mockId);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals(tasteProfileId, updatedProfile.getId());
        assertEquals(newGenres, updatedProfile.getTopGenres());

        // Verify that the update method was called internally.
        verify(tasteProfileCalculationService, times(1)).calculateTopGenres(user);
        verify(tasteProfileCalculationService, times(1)).calculateTopArtists(user);
        verify(tasteProfileCalculationService, times(1)).calculateDiscoveryPattern(user);
        verify(tasteProfileCalculationService, times(1)).calculateMainStreamScore(user);

        // Ensure the save method was called with the existing profile object.
        verify(tasteProfileCommandService, times(1)).saveTasteProfile(existingProfile);
    }
}

// NOTE: The buildTasteProfile test is now redundant because it's a simple constructor call and is implicitly tested by the createUserTasteProfile_shouldReturnSavedProfileOnCreation test.