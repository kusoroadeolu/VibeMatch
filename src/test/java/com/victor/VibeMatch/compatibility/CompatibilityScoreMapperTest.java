package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.dtos.ArtistCompatibilityDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityUserDto;
import com.victor.VibeMatch.compatibility.dtos.GenreCompatibilityDto;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityKey;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.math.MathUtils;
import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompatibilityScoreMapperTest {

    @Mock
    private MathUtils mathUtils; // Mock the MathUtils dependency

    @InjectMocks
    private CompatibilityScoreMapper compatibilityScoreMapper; // Inject the mapper to be tested

    private User user1;
    private User user2;
    private CompatibilityScore compatibilityScore;
    private List<CompatibilityWrapper> sharedArtists;
    private List<CompatibilityWrapper> sharedGenres;
    private List<String> compatibilityReasons;

    @BeforeEach
    void setUp() {
        // Initialize mock users
        user1 = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .username("Alice")
                .build();

        user2 = User.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .username("Bob")
                .build();

        // Initialize shared artists data
        sharedArtists = Arrays.asList(
                CompatibilityWrapper.builder().name("Artist A").your(10.0).their(15.0).build(),
                CompatibilityWrapper.builder().name("Artist B").your(5.0).their(8.0).build()
        );

        // Initialize shared genres data
        sharedGenres = Arrays.asList(
                CompatibilityWrapper.builder().name("Pop").your(0.45).their(0.50).build()
        );

        // Initialize compatibility reasons
        compatibilityReasons = Arrays.asList(
                "You have 2 artists in common.",
                "You both love Pop.",
                "You have similar music discovery patterns."
        );

        // Initialize the CompatibilityScore entity
        compatibilityScore = CompatibilityScore.builder()
                .key(CompatibilityKey.builder().userId(user1.getId()).targetUserId(user2.getId()).build())
                .user(user1)
                .targetUser(user2)
                .discoveryCompatibility(0.789)
                .tasteCompatibility(0.812)
                .sharedArtists(sharedArtists)
                .sharedGenres(sharedGenres)
                .compatibilityReasons(compatibilityReasons)
                .lastCalculated(LocalDateTime.of(2023, 1, 15, 10, 30, 0))
                .build();

    }

    @Test
    void responseDto_shouldMapAllFieldsCorrectly() {
        //Arrange
        when(mathUtils.round(0.789)).thenReturn(0.79);
        when(mathUtils.round(0.812)).thenReturn(0.81);
        when(mathUtils.round(0.45)).thenReturn(0.45);
        when(mathUtils.round(0.50)).thenReturn(0.50);

        // Act
        CompatibilityScoreResponseDto dto = compatibilityScoreMapper.responseDto(compatibilityScore);

        // Assert
        assertNotNull(dto);
        assertEquals(user1.getId().toString(), dto.user().userId());
        assertEquals(user1.getUsername(), dto.user().username());
        assertEquals(user2.getId().toString(), dto.targetUser().userId());
        assertEquals(user2.getUsername(), dto.targetUser().username());

        // Verify rounding was applied
        assertEquals(0.79, dto.discoveryCompatibilityScore());
        assertEquals(0.81, dto.tasteCompatibilityScore());

        assertEquals(2, dto.sharedArtists().size());
        assertEquals("Artist A", dto.sharedArtists().getFirst().artistName());
        assertEquals(10, dto.sharedArtists().getFirst().yourRank());
        assertEquals(15, dto.sharedArtists().getFirst().theirRank());

        assertEquals(1, dto.sharedGenres().size());
        assertEquals("Pop", dto.sharedGenres().getFirst().genreName());
        assertEquals(0.45, dto.sharedGenres().getFirst().yourPercentage());
        assertEquals(0.50, dto.sharedGenres().getFirst().theirPercentage());

        assertEquals(compatibilityReasons, dto.whyCompatible());
        assertEquals(compatibilityScore.getLastCalculated(), dto.lastCalculated());

        // Verify that mathUtils.round was called for relevant fields
        verify(mathUtils, times(4)).round(anyDouble());
    }

    @Test
    void userDto_shouldMapUserToDtoCorrectly() {
        // Act
        CompatibilityUserDto dto = compatibilityScoreMapper.userDto(user1);

        // Assert
        assertNotNull(dto);
        assertEquals(user1.getId().toString(), dto.userId());
        assertEquals(user1.getUsername(), dto.username());
    }

    @Test
    void artistDtos_shouldMapListCorrectly() {

        // Act
        List<ArtistCompatibilityDto> dtos = compatibilityScoreMapper.artistDtos(sharedArtists);

        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Artist A", dtos.get(0).artistName());
        assertEquals(10, dtos.get(0).yourRank());
        assertEquals(15, dtos.get(0).theirRank());
        assertEquals("Artist B", dtos.get(1).artistName());
        assertEquals(5, dtos.get(1).yourRank());
        assertEquals(8, dtos.get(1).theirRank());
    }

    @Test
    void genreDtos_shouldMapListCorrectlyAndApplyRounding() {
        //Arrange
        when(mathUtils.round(0.45)).thenReturn(0.45);
        when(mathUtils.round(0.50)).thenReturn(0.50);

        // Act
        List<GenreCompatibilityDto> dtos = compatibilityScoreMapper.genreDtos(sharedGenres);

        // Assert
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals("Pop", dtos.get(0).genreName());
        assertEquals(0.45, dtos.get(0).yourPercentage());
        assertEquals(0.50, dtos.get(0).theirPercentage());

        verify(mathUtils, times(2)).round(anyDouble());
    }
}
