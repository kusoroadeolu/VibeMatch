package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.*;
import com.victor.VibeMatch.compatibility.dtos.ArtistCompatibilityDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityUserDto;
import com.victor.VibeMatch.compatibility.dtos.GenreCompatibilityDto;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityKey;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.exceptions.UserPrivacyException;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompatibilityScorePersistenceServiceImplTest {

    @Mock
    private CompatibilityScoreCreationService compatibilityScoreCreationService;
    @Mock
    private CompatibilityScoreCommandService compatibilityScoreCommandService;
    @Mock
    private CompatibilityScoreQueryService compatibilityScoreQueryService;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private CompatibilityScoreMapper compatibilityScoreMapper;

    @InjectMocks
    @Spy
    private CompatibilityScorePersistenceServiceImpl compatibilityPersistenceService;

    private User user;
    private User targetUser;
    private UUID userId;
    private UUID targetUserId;
    private List<CompatibilityWrapper> mockSharedArtists;
    private List<CompatibilityWrapper> mockSharedGenres;
    private double mockDiscoveryCompatibility;
    private double mockTasteCompatibility;
    private List<String> mockCompatibilityReasons;
    private CompatibilityScore mockCompatibilityScoreEntity;
    private CompatibilityScoreResponseDto mockResponseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1");
        targetUserId = UUID.fromString("d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2");

        user = User.builder().id(userId).username("TestUser").build();
        targetUser = User.builder().id(targetUserId).username("TargetUser").isPublic(true).build();

        mockSharedArtists = Collections.singletonList(
                CompatibilityWrapper.builder().name("Artist X").your(5.0).their(10.0).build()
        );
        mockSharedGenres = Collections.singletonList(
                CompatibilityWrapper.builder().name("Pop").your(0.3).their(0.35).build()
        );
        mockDiscoveryCompatibility = 0.85;
        mockTasteCompatibility = 0.75;
        mockCompatibilityReasons = Arrays.asList(
                "You have 1 artists in common.",
                "You both love Pop.",
                "You have very similar music discovery patterns."
        );

        mockCompatibilityScoreEntity = CompatibilityScore.builder()
                .key(CompatibilityKey.builder().userId(userId).targetUserId(targetUserId).build())
                .user(user)
                .targetUser(targetUser)
                .discoveryCompatibility(mockDiscoveryCompatibility)
                .tasteCompatibility(mockTasteCompatibility)
                .sharedArtists(mockSharedArtists)
                .sharedGenres(mockSharedGenres)
                .compatibilityReasons(mockCompatibilityReasons)
                .lastCalculated(LocalDateTime.now())
                .build();

        mockResponseDto = new CompatibilityScoreResponseDto(
                new CompatibilityUserDto(userId.toString(), "TestUser"),
                new CompatibilityUserDto(targetUserId.toString(), "TargetUser"),
                mockDiscoveryCompatibility,
                mockTasteCompatibility,
                List.of(new ArtistCompatibilityDto("Artist X", 5, 10)),
                List.of(new GenreCompatibilityDto("Pop", 0.3, 0.35)),
                mockCompatibilityReasons,
                LocalDateTime.now()
        );
    }


    @Test
    void getCompatibilityScore_shouldDeleteOldScoreAndSaveNewScore() {
        // Arrange
        when(userQueryService.findByUserId(userId)).thenReturn(user);
        when(userQueryService.findByUserId(targetUserId)).thenReturn(targetUser);
        doReturn(mockResponseDto).when(compatibilityPersistenceService).saveCompatibilityScore(user, targetUser);

        // Act
        CompatibilityScoreResponseDto resultDto = compatibilityPersistenceService.getCompatibilityScore(userId, targetUserId);

        // Assert
        assertNotNull(resultDto);
        assertEquals(mockResponseDto, resultDto);
        verify(compatibilityScoreCommandService, times(1)).deleteByUserAndTargetUser(user, targetUser);
        verify(compatibilityPersistenceService, times(1)).saveCompatibilityScore(user, targetUser);
    }

    @Test
    void saveCompatibilityScore_shouldCallDependenciesAndReturnDto() {
        //Arrange
        when(compatibilityScoreCreationService.getSharedArtists(user, targetUser)).thenReturn(mockSharedArtists);
        when(compatibilityScoreCreationService.getSharedGenres(user, targetUser)).thenReturn(mockSharedGenres);
        when(compatibilityScoreCreationService.getDiscoveryCompatibility(user, targetUser)).thenReturn(mockDiscoveryCompatibility);
        when(compatibilityScoreCreationService.getTasteCompatibility(user, targetUser)).thenReturn(mockTasteCompatibility);
        when(compatibilityScoreCreationService.buildCompatibilityReasons(mockSharedArtists, mockSharedGenres, mockDiscoveryCompatibility)).thenReturn(mockCompatibilityReasons);
        when(compatibilityScoreCommandService.saveCompatibilityScore(any(CompatibilityScore.class))).thenReturn(mockCompatibilityScoreEntity);
        when(compatibilityScoreMapper.responseDto(any(CompatibilityScore.class))).thenReturn(mockResponseDto);

        // Act
        CompatibilityScoreResponseDto resultDto = compatibilityPersistenceService.saveCompatibilityScore(user, targetUser);

        // Assert
        assertNotNull(resultDto);
        assertEquals(mockResponseDto, resultDto);

        verify(compatibilityScoreCreationService, times(1)).getSharedArtists(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).getSharedGenres(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).getDiscoveryCompatibility(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).getTasteCompatibility(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).buildCompatibilityReasons(mockSharedArtists, mockSharedGenres, mockDiscoveryCompatibility);

        ArgumentCaptor<CompatibilityScore> scoreCaptor = ArgumentCaptor.forClass(CompatibilityScore.class);
        verify(compatibilityScoreCommandService, times(1)).saveCompatibilityScore(scoreCaptor.capture());
        CompatibilityScore capturedScore = scoreCaptor.getValue();

        assertNotNull(capturedScore);
        assertEquals(user.getId(), capturedScore.getUser().getId());
        assertEquals(targetUser.getId(), capturedScore.getTargetUser().getId());
        assertEquals(mockDiscoveryCompatibility, capturedScore.getDiscoveryCompatibility());
        assertEquals(mockTasteCompatibility, capturedScore.getTasteCompatibility());
        assertEquals(mockSharedArtists, capturedScore.getSharedArtists());
        assertEquals(mockSharedGenres, capturedScore.getSharedGenres());
        assertEquals(mockCompatibilityReasons, capturedScore.getCompatibilityReasons());
    }


    @Test
    void saveCompatibilityScore_shouldNotSaveIfScoreIsTooLow() {
        // Arrange
        double lowTasteScore = 0.5;
        double highDiscoveryScore = 0.8;
        targetUser.setPublic(true);

        when(compatibilityScoreCreationService.getTasteCompatibility(user, targetUser)).thenReturn(lowTasteScore);
        when(compatibilityScoreCreationService.getDiscoveryCompatibility(user, targetUser)).thenReturn(highDiscoveryScore);
        when(compatibilityScoreCreationService.getSharedArtists(any(), any())).thenReturn(Collections.emptyList());
        when(compatibilityScoreCreationService.getSharedGenres(any(), any())).thenReturn(Collections.emptyList());
        when(compatibilityScoreCreationService.buildCompatibilityReasons(any(), any(), anyDouble())).thenReturn(Collections.emptyList());
        when(compatibilityScoreMapper.responseDto(any(CompatibilityScore.class))).thenReturn(mockResponseDto);

        // Act
        compatibilityPersistenceService.saveCompatibilityScore(user, targetUser);

        // Assert
        verify(compatibilityScoreCommandService, never()).saveCompatibilityScore(any(CompatibilityScore.class));
        verify(compatibilityScoreMapper, times(1)).responseDto(any(CompatibilityScore.class));
    }


    @Test
    void saveCompatibilityScore_shouldThrowExceptionForPrivateUser() {
        // Arrange
        targetUser.setPublic(false);

        // Act & Assert
        assertThrows(UserPrivacyException.class, () -> {
            compatibilityPersistenceService.saveCompatibilityScore(user, targetUser);
        });

        verifyNoInteractions(compatibilityScoreCreationService);
        verifyNoInteractions(compatibilityScoreCommandService);
        verifyNoInteractions(compatibilityScoreMapper);
    }

    @Test
    void buildCompatibilityScore_shouldConstructCorrectEntity() {
        // Arrange
        when(compatibilityScoreCreationService.getSharedArtists(user, targetUser)).thenReturn(mockSharedArtists);
        when(compatibilityScoreCreationService.getSharedGenres(user, targetUser)).thenReturn(mockSharedGenres);
        when(compatibilityScoreCreationService.getDiscoveryCompatibility(user, targetUser)).thenReturn(mockDiscoveryCompatibility);
        when(compatibilityScoreCreationService.getTasteCompatibility(user, targetUser)).thenReturn(mockTasteCompatibility);
        when(compatibilityScoreCreationService.buildCompatibilityReasons(mockSharedArtists, mockSharedGenres, mockDiscoveryCompatibility))
                .thenReturn(mockCompatibilityReasons);

        //Act
        CompatibilityScore builtScore = compatibilityPersistenceService.buildCompatibilityScore(user, targetUser);

        // Assert
        assertNotNull(builtScore);
        assertEquals(user, builtScore.getUser());
        assertEquals(targetUser, builtScore.getTargetUser());
        assertEquals(mockDiscoveryCompatibility, builtScore.getDiscoveryCompatibility());
        assertEquals(mockTasteCompatibility, builtScore.getTasteCompatibility());
        assertEquals(mockSharedArtists, builtScore.getSharedArtists());
        assertEquals(mockSharedGenres, builtScore.getSharedGenres());
        assertEquals(mockCompatibilityReasons, builtScore.getCompatibilityReasons());

        verify(compatibilityScoreCreationService, times(1)).getSharedArtists(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).getSharedGenres(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).getDiscoveryCompatibility(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).getTasteCompatibility(user, targetUser);
        verify(compatibilityScoreCreationService, times(1)).buildCompatibilityReasons(mockSharedArtists, mockSharedGenres, mockDiscoveryCompatibility);

        verifyNoInteractions(compatibilityScoreCommandService);
        verifyNoInteractions(compatibilityScoreMapper);
    }

    @Test
    void deleteCompatibilityScoresByUser_shouldSuccessfullyCallCommandService_toDeleteCompatibilityScores(){
        // Act
        compatibilityPersistenceService.deleteCompatibilityScoresByUser(user);

        // Assert
        verify(compatibilityScoreCommandService, times(1)).deleteCompatibilityScoresByUser(user);
    }

}