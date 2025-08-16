package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.cache.TasteProfileCacheServiceImpl;
import com.victor.VibeMatch.exceptions.NoSuchUserException;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TasteProfileServiceImpl implements TasteProfileService {
    private final TasteProfileCreationService tasteProfileCreationService;
    private final TasteProfileCacheServiceImpl tasteProfileCacheService;
    private final TasteProfileMapper tasteProfileMapper;

    @Override
    public TasteProfileResponseDto createTasteProfile(UUID userId){
        TasteProfile tasteProfile = tasteProfileCreationService.createUserTasteProfile(userId);
        var responseDto = tasteProfileMapper.responseDto(tasteProfile);
        tasteProfileCacheService.cacheTasteProfile(userId.toString(), responseDto);
        log.info("Successfully cached taste profile for user with ID: {}", userId);
        return responseDto;
    }

}
