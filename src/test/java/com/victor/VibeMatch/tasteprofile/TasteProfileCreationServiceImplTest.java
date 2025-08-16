package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.tasteprofile.impl.TasteProfileCreationServiceImpl;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasteProfileCreationServiceImplTest {

    @Mock
    private TasteProfileCalculationService tasteProfileCalculationService;

    @Mock
    private TasteProfileCommandService tasteProfileCommandService;

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private TasteProfileCreationServiceImpl tasteProfileCreationService;

    private TasteProfile tasteProfile;

    private User user;

    @BeforeEach
    public void setUp(){
        tasteProfile = TasteProfile
                .builder()
                .topGenres(List.of())
                .topArtists(List.of())
                .mainstreamScore(0.7d)
                .discoveryPattern(0.8d)
                .build();

        user = User
                .builder()
                .username("mock-name")
                .build();
    }

    @Test
    void createUserTasteProfile_shouldReturnSavedProfileOnCreation() {
        //Arrange
        UUID mockId = UUID.randomUUID();
        when(userQueryService.findByUserId(mockId)).thenReturn(user);
        when(tasteProfileCalculationService.calculateTopGenres(user)).thenReturn(List.of());
        when(tasteProfileCalculationService.calculateTopArtists(user)).thenReturn(List.of());
        when(tasteProfileCalculationService.calculateDiscoveryPattern(user)).thenReturn(0.7d);
        when(tasteProfileCalculationService.calculateMainStreamScore(user)).thenReturn(0.8d);
        when(tasteProfileCommandService.saveTasteProfile(any(TasteProfile.class))).thenReturn(tasteProfile);

        //Act
        TasteProfile savedProfile = tasteProfileCreationService.createUserTasteProfile(mockId);

        //Assert
        assertNotNull(savedProfile);
        assertEquals(tasteProfile, savedProfile);
        verify(tasteProfileCalculationService, times(1)).calculateTopGenres(user);
        verify(tasteProfileCalculationService, times(1)).calculateTopArtists(user);
        verify(tasteProfileCalculationService, times(1)).calculateDiscoveryPattern(user);
        verify(tasteProfileCalculationService, times(1)).calculateMainStreamScore(user);
        verify(tasteProfileCommandService, times(1)).saveTasteProfile(any(TasteProfile.class));
    }

    @Test
    void buildTasteProfile_shouldReturnBuiltTasteProfile() {
        //Arrange
        List<TasteWrapper> genreWrapper = List.of();
        List<TasteWrapper> artistWrapper = List.of();
        double discoveryPattern = 0.8d;
        double mainstreamScore = 0.7d;

        //Act
        TasteProfile mockProfile = tasteProfileCreationService.buildTasteProfile(user, genreWrapper, artistWrapper, mainstreamScore, discoveryPattern);

        //Assert
        assertNotNull(mockProfile);
        assertSame(genreWrapper, mockProfile.getTopGenres());
        assertSame(artistWrapper, mockProfile.getTopArtists());
        assertEquals(discoveryPattern, mockProfile.getDiscoveryPattern());
        assertEquals(mainstreamScore, mockProfile.getMainstreamScore());

    }
}