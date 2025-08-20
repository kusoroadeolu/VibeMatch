package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScoreRepository;
import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompatibilityScoreQueryServiceImplTest {

    @Mock
    private CompatibilityScoreRepository compatibilityScoreRepository;

    @InjectMocks
    private CompatibilityScoreScoreQueryServiceImpl compatibilityScoreQueryService;

    private CompatibilityScore compatibilityScore;
    private User user;
    private User targetUser;

    @BeforeEach
    public void setUp(){
        user = User.builder().username("user").build();
        targetUser = User.builder().username("target_user").build();

        compatibilityScore = CompatibilityScore
                .builder()
                .user(user)
                .targetUser(targetUser)
                .discoveryCompatibility(0.6d)
                .tasteCompatibility(0.7d)
                .sharedArtists(List.of())
                .sharedGenres(List.of())
                .compatibilityReasons(List.of())
                .lastCalculated(LocalDateTime.now())
                .build();
    }


    @Test
    void findByUserAndTargetUser_shouldReturnCompatibilityScore_givenUserAndTargetUser() {
        //Arrange
        //
        when(compatibilityScoreRepository.findByUserAndTargetUser(user, targetUser)).thenReturn(compatibilityScore);

        //Act
        CompatibilityScore expected = compatibilityScoreQueryService.findByUserAndTargetUser(user, targetUser);

        //Assert
        assertNotNull(expected);
        assertEquals(compatibilityScore, expected);
        verify(compatibilityScoreRepository, times(1)).findByUserAndTargetUser(user, targetUser);
    }

    @Test
    void existsByUserAndTargetUser_shouldReturnTrue_givenExistingScore() {
        // Arrange
        when(compatibilityScoreRepository.existsByUserAndTargetUser(user, targetUser)).thenReturn(true);

        // Act
        boolean exists = compatibilityScoreQueryService.existsByUserAndTargetUser(user, targetUser);

        assertTrue(exists);
        verify(compatibilityScoreRepository, times(1)).existsByUserAndTargetUser(user, targetUser);
    }

    @Test
    void existsByUserAndTargetUser_shouldReturnFalse_givenNonExistingScore() {
        // Arrange
        when(compatibilityScoreRepository.existsByUserAndTargetUser(user, targetUser)).thenReturn(false);

        // Act
        boolean exists = compatibilityScoreQueryService.existsByUserAndTargetUser(user, targetUser);

        // Assert
        assertFalse(exists);
        verify(compatibilityScoreRepository, times(1)).existsByUserAndTargetUser(user, targetUser);
    }
}