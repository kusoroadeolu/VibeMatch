package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.compatibility.CompatibilityScoreRepository;
import com.victor.VibeMatch.exceptions.CompatibilityScoreDeleteException;
import com.victor.VibeMatch.exceptions.CompatibilityScoreSaveException;
import com.victor.VibeMatch.user.User;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompatibilityScoreCommandServiceImplTest {

    @Mock
    private CompatibilityScoreRepository compatibilityScoreRepository;

    @InjectMocks
    private CompatibilityScoreCommandServiceImpl compatibilityScoreCommandService;

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
    void saveCompatibilityScore_shouldSuccessfullySaveCompatibilityScore() {
        //Arrange
        when(compatibilityScoreRepository.save(any(CompatibilityScore.class))).thenReturn(compatibilityScore);

        //Act
        CompatibilityScore savedScore = compatibilityScoreCommandService.saveCompatibilityScore(compatibilityScore);

        //Assert
        assertNotNull(savedScore);
        assertEquals(compatibilityScore, savedScore);
        verify(compatibilityScoreRepository, times(1)).save(compatibilityScore);

    }

    @Test
    void saveCompatibilityScore_shouldThrowCompatibilityScoreEx_onGenericEx() {
        //Arrange
        RuntimeException runtimeException = new RuntimeException();
        when(compatibilityScoreRepository.save(any(CompatibilityScore.class))).thenThrow(runtimeException);

        //Act & Assert
        var ex = assertThrows(CompatibilityScoreSaveException.class, () -> {
            compatibilityScoreCommandService.saveCompatibilityScore(compatibilityScore);
        });

        assertEquals("An error occurred while trying to save the compatibility score", ex.getMessage());
        assertEquals(runtimeException, ex.getCause());
        verify(compatibilityScoreRepository, times(1)).save(compatibilityScore);

    }

    @Test
    void deleteCompatibilityScore_shouldSuccessfullyDeleteAllScores_givenUser(){
        //Arrange
        User user = User.builder().id(UUID.randomUUID()).username("mock-name").build();

        //Act
        compatibilityScoreCommandService.deleteCompatibilityScoresByUser(user);

        //Assert
        verify(compatibilityScoreRepository, times(1)).deleteByKeyUserId(user.getId());

    }

    @Test
    void deleteCompatibilityScore_shouldThrowDeleteEx_OnGenericException(){
        //Arrange
        User user = User.builder().id(UUID.randomUUID()).username("mock-name").build();
        doThrow(new RuntimeException()).when(compatibilityScoreRepository).deleteByKeyUserId(user.getId());

        //Act & Assert
        assertThrows(CompatibilityScoreDeleteException.class, () ->
                compatibilityScoreCommandService.deleteCompatibilityScoresByUser(user)
        );
        verify(compatibilityScoreRepository, times(1)).deleteByKeyUserId(user.getId());

    }
}