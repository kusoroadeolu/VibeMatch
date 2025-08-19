package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScorePersistenceService;
import com.victor.VibeMatch.compatibility.dtos.ArtistCompatibilityDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityUserDto;
import com.victor.VibeMatch.compatibility.dtos.GenreCompatibilityDto;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompatibilityScoreBatchServiceImplTest {

    @Mock
    private UserQueryService userQueryService;
    @Mock
    private CompatibilityScorePersistenceService compatibilityScorePersistenceService;
    @InjectMocks
    private CompatibilityScoreBatchServiceImpl compatibilityScoreBatchService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void returnAllCompatibleUsersInBatch() {
        //Arrange
        UUID userId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User targetUser = User.builder().id(targetUserId).isPublic(true).build();
        var mockResponseDto = new CompatibilityScoreResponseDto(
                new CompatibilityUserDto(userId.toString(), "TestUser"),
                new CompatibilityUserDto(targetUserId.toString(), "TargetUser"),
                .72,
                .88,
                List.of(new ArtistCompatibilityDto("Artist X", 5, 10)),
                List.of(new GenreCompatibilityDto("Pop", 0.3, 0.35)),
                List.of(),
                LocalDateTime.now() // Mock mapper's lastCalculated
        );
        when(userQueryService.findByUserId(userId)).thenReturn(user);
        when(userQueryService.findAllUsers()).thenReturn(List.of(targetUser));
        when(compatibilityScorePersistenceService.saveCompatibilityScore(user, targetUser)).thenReturn(mockResponseDto);

        //Act
        List<CompatibilityScoreResponseDto> dtos = compatibilityScoreBatchService.findCompatibleUsersInBatch(userId);

        //Assert
        assertNotNull(dtos);
        assertEquals(dtos.getFirst(), mockResponseDto);
        verify(compatibilityScorePersistenceService, times(1)).deleteCompatibilityScoresByUser(user);

    }
}