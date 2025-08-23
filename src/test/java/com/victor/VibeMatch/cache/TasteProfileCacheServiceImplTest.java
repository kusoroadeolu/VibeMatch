package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.tasteprofile.*;
import com.victor.VibeMatch.tasteprofile.dto.ArtistDto;
import com.victor.VibeMatch.tasteprofile.dto.GenreDto;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TasteProfileCacheServiceImplTest {

    @MockitoBean
    private TasteProfilePersistenceService tasteProfilePersistenceService;

    @MockitoBean
    private TasteProfileMapper tasteProfileMapper;

    @Autowired
    private TasteProfileCacheServiceImpl tasteProfileCacheService;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;


    private TasteProfileResponseDto tasteProfile;

    @BeforeEach
    public void setUp(){
        tasteProfile = new TasteProfileResponseDto(
                "user123",                          // userId
                "JaneDoe",                          // username
                true,                               // isPublic
                List.of(                            // topGenres
                        new GenreDto("Pop", 55.5, 10),
                        new GenreDto("Rock", 30.0, 5)
                ),
                List.of(                            // topArtists
                        new ArtistDto("Queen", 1),
                        new ArtistDto("Taylor Swift", 2)
                ),
                0.75,                               // mainstreamScore
                "Loyalist",                               // howYouListen
                LocalDateTime.now()                 // lastUpdated
        );


        cache = cacheManager.getCache("tasteProfileCache");

        if(cache != null){
            cache.clear();
        }

    }


    @Test
    void cacheTasteProfile_shouldCacheTasteProfile() {
        //Arrange
        String mockId = "mock-id-put";

        //Act
        TasteProfileResponseDto mockProfile = tasteProfileCacheService.cacheTasteProfile(mockId, tasteProfile);

        //Assert
        TasteProfileResponseDto cachedValue = cache.get(mockId, TasteProfileResponseDto.class);
        assertNotNull(mockProfile);
        assertNotNull(cachedValue);
        assertEquals(mockProfile.userId(), cachedValue.userId());
        assertEquals(mockProfile.howYouListen(), cachedValue.howYouListen());

    }

    @Test
    void getCachedProfile_shouldReturnProfileFromCache_onSecondCall() {
        //Arrange
        UUID mockId = UUID.randomUUID();
        when(tasteProfilePersistenceService.createUserTasteProfile(mockId)).thenReturn(new TasteProfile());
        when(tasteProfileMapper.responseDto(any(TasteProfile.class))).thenReturn(tasteProfile);

        //Assert
        assertNull(cache.get(mockId));

        //Act
        TasteProfileResponseDto mockProfile = tasteProfileCacheService.getCachedProfile(mockId.toString());

        //Assert
        assertNotNull(mockProfile);

        //Act
        TasteProfileResponseDto cachedProfile = tasteProfileCacheService.getCachedProfile(mockId.toString());

        //Assert
        assertNotNull(cachedProfile);
        assertEquals(mockProfile.userId(), cachedProfile.userId());
        assertEquals(mockProfile.howYouListen(), cachedProfile.howYouListen());

    }

    @Test
    void evictTasteProfile() {
        //Arrange
        String mockId = "mock-id-evict";
         tasteProfileCacheService.cacheTasteProfile(mockId, tasteProfile);

        //Assert
        assertNotNull(cache.get(mockId));

        //Act
        tasteProfileCacheService.evictTasteProfile(mockId);

        //Assert
        assertNull(cache.get(mockId));

    }
}