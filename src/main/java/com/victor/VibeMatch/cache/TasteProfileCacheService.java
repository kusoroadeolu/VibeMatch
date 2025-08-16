package com.victor.VibeMatch.cache;

import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public interface TasteProfileCacheService {
    TasteProfileResponseDto cacheTasteProfile(String userId, TasteProfileResponseDto tasteProfile);

    TasteProfileResponseDto getCachedProfile(String userId);

    void evictTasteProfile(String userId);
}
