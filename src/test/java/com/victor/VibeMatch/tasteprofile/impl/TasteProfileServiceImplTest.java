package com.victor.VibeMatch.tasteprofile.impl;

import com.victor.VibeMatch.cache.TasteProfileCacheServiceImpl;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileMapper;
import com.victor.VibeMatch.tasteprofile.TasteProfilePersistenceService;
import com.victor.VibeMatch.tasteprofile.dto.ArtistDto;
import com.victor.VibeMatch.tasteprofile.dto.GenreDto;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasteProfileServiceImplTest {

    @Mock
    private TasteProfilePersistenceService tasteProfilePersistenceService;

    @Mock
    private TasteProfileCacheServiceImpl tasteProfileCacheService;

    @Mock
    private TasteProfileMapper tasteProfileMapper;

    @InjectMocks
    private TasteProfileServiceImpl tasteProfileService;

    private UUID mockUserId;
    private TasteProfile mockTasteProfile;
    private TasteProfileResponseDto mockResponseDto;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockTasteProfile = TasteProfile.builder().id(UUID.randomUUID()).build();

        // Use the full DTO structure with some sample data
        mockResponseDto = new TasteProfileResponseDto(
                mockUserId.toString(),
                "testUser",
                true,
                List.of(new GenreDto("Rock", 0.7, 5)), // Updated GenreDto with artistCount
                List.of(new ArtistDto("Radiohead", 1)), // Updated ArtistDto with rank
                0.8,
                "Explorer",
                LocalDateTime.now()
        );
    }

    @Test
    void createTasteProfile_shouldCreateAndCacheProfile() {
        // Arrange
        when(tasteProfilePersistenceService.createUserTasteProfile(mockUserId))
                .thenReturn(mockTasteProfile);
        when(tasteProfileMapper.responseDto(mockTasteProfile))
                .thenReturn(mockResponseDto);

        // Act
        TasteProfileResponseDto result = tasteProfileService.createTasteProfile(mockUserId);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponseDto, result);

        // Verify that the dependencies were called in the correct order
        verify(tasteProfilePersistenceService, times(1)).createUserTasteProfile(mockUserId);
        verify(tasteProfileMapper, times(1)).responseDto(mockTasteProfile);
        verify(tasteProfileCacheService, times(1)).cacheTasteProfile(mockUserId.toString(), mockResponseDto);
    }


    @Test
    void getTasteProfile_shouldReturnCachedProfile() {
        // Arrange
        when(tasteProfileCacheService.getCachedProfile(mockUserId.toString()))
                .thenReturn(mockResponseDto);

        // Act
        TasteProfileResponseDto result = tasteProfileService.getTasteProfile(mockUserId);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponseDto, result);

        // Verify that only the cache service was called
        verify(tasteProfileCacheService, times(1)).getCachedProfile(mockUserId.toString());
        verifyNoInteractions(tasteProfilePersistenceService, tasteProfileMapper);
    }


    @Test
    void removeTasteProfile_shouldEvictFromCache() {
        // Arrange - no specific mocks needed as the method returns void

        // Act
        tasteProfileService.removeTasteProfile(mockUserId);

        // Assert
        verify(tasteProfileCacheService, times(1)).evictTasteProfile(mockUserId.toString());
    }
}