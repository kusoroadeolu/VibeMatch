package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileCreationService;
import com.victor.VibeMatch.tasteprofile.TasteProfileMapper;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TasteProfileCacheServiceImpl implements TasteProfileCacheService {

    private final TasteProfileCreationService tasteProfileCreationService;
    private final TasteProfileMapper tasteProfileMapper;

    @CachePut(key = "#userId", cacheNames = "tasteProfileCache")
    @Override
    public TasteProfileResponseDto cacheTasteProfile(String userId, TasteProfileResponseDto tasteProfile){
        return tasteProfile;
    }

    @Cacheable(key = "#userId", cacheNames = "tasteProfileCache")
    @Override
    public TasteProfileResponseDto getCachedProfile(String userId){
        TasteProfile profile = tasteProfileCreationService.createUserTasteProfile(UUID.fromString(userId));
        return tasteProfileMapper.responseDto(profile);
    }

    @CacheEvict(key = "#userId", cacheNames = "tasteProfileCache")
    @Override
    public void evictTasteProfile(String userId){

    }


}
