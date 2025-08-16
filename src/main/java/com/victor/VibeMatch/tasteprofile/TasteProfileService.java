package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;

import java.util.UUID;

public interface TasteProfileService {
    TasteProfileResponseDto createTasteProfile(UUID userId);
}
