package com.victor.VibeMatch.tasteprofile.impl;

import com.victor.VibeMatch.cache.TasteProfileCacheServiceImpl;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileMapper;
import com.victor.VibeMatch.tasteprofile.TasteProfilePersistenceService;
import com.victor.VibeMatch.tasteprofile.TasteProfileService;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TasteProfileServiceImpl implements TasteProfileService {
    private final TasteProfilePersistenceService tasteProfilePersistenceService;
    private final TasteProfileCacheServiceImpl tasteProfileCacheService;
    private final TasteProfileMapper tasteProfileMapper;

    @Override
    public TasteProfileResponseDto createTasteProfile(UUID userId){
        TasteProfile tasteProfile = tasteProfilePersistenceService.createUserTasteProfile(userId);
        var responseDto = tasteProfileMapper.responseDto(tasteProfile);
        tasteProfileCacheService.cacheTasteProfile(userId.toString(), responseDto);
        log.info("Successfully cached taste profile for user with ID: {}", userId);
        return responseDto;
    }

    /**
     * Gets a taste profile for a user
     * @param userId The user
     * @return A dto of the user's taste profile
     * */
    @Override
    public TasteProfileResponseDto getTasteProfile(UUID userId){
        TasteProfileResponseDto tasteProfileResponseDto =
                tasteProfileCacheService.getCachedProfile(userId.toString());
        log.info("Found a taste profile with for user with ID: {}", userId);
        return tasteProfileResponseDto;
    }

    /**
     * Removes a taste profile for a user from the cache
     * @param userId The user
     * */
    @Override
    public void removeTasteProfile(UUID userId){
        tasteProfileCacheService.evictTasteProfile(userId.toString());
        log.info("Successfully evicted taste profile for user with ID: {}", userId);
    }

}
