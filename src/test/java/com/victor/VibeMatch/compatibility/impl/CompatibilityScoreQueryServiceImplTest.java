package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScoreRepository;
import com.victor.VibeMatch.exceptions.NoSuchUserException;
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

    @BeforeEach
    public void setUp(){
        compatibilityScore = CompatibilityScore
                .builder()
                .user(User.builder().username("user").build())
                .targetUser(User.builder().username("target_user").build())
                .discoveryCompatibility(0.6d)
                .tasteCompatibility(0.7d)
                .sharedArtists(List.of())
                .sharedGenres(List.of())
                .compatibilityReasons(List.of())
                .lastCalculated(LocalDateTime.now())
                .build();
    }


    @Test
    void findByUserIdAndTargetId_shouldReturnCompatibilityScore_givenUserIdAndTargetId() {
        //Arrange
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(compatibilityScoreRepository.findByKeyUserIdAndTargetUserId(userId, targetId)).thenReturn(compatibilityScore);

        //Act
        CompatibilityScore expected = compatibilityScoreQueryService.findByUserIdAndTargetId(userId, targetId);

        //Assert
        assertNotNull(expected);
        assertEquals(compatibilityScore, expected);
        verify(compatibilityScoreRepository, times(1)).findByKeyUserIdAndTargetUserId(userId, targetId);
    }

    @Test
    public void findByUserIdAndTargetId_shouldThrowNoSuchUserEx_givenNullUserId(){
        //Arrange
        UUID userId = null;
        UUID targetId = UUID.randomUUID();

        //Act & Assert
         var ex = assertThrows(NoSuchUserException.class, () -> {
             compatibilityScoreQueryService.findByUserIdAndTargetId(userId, targetId);
         });
         assertEquals("User ID or Target ID cannot be null", ex.getMessage());
         verify(compatibilityScoreRepository, never()).findByKeyUserIdAndTargetUserId(userId, targetId);
    }

    @Test
    public void findByUserIdAndTargetId_shouldThrowNoSuchUserEx_givenNullTargetId(){
        //Arrange
        UUID userId = UUID.randomUUID();
        UUID targetId = null;

        //Act & Assert
        var ex = assertThrows(NoSuchUserException.class, () -> {
            compatibilityScoreQueryService.findByUserIdAndTargetId(userId, targetId);
        });
        assertEquals("User ID or Target ID cannot be null", ex.getMessage());
        verify(compatibilityScoreRepository, never()).findByKeyUserIdAndTargetUserId(userId, targetId);
    }
}