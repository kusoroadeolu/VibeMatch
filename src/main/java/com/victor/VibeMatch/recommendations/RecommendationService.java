package com.victor.VibeMatch.recommendations;

import com.victor.VibeMatch.recommendations.dtos.RecommendationRequestDto;
import com.victor.VibeMatch.recommendations.dtos.RecommendationResponseDto;

import java.util.List;
import java.util.UUID;

public interface RecommendationService {
    // Method to handle sending a new recommendation
    void sendRecommendation(UUID recommenderId, UUID receiverId, RecommendationRequestDto requestDto);

    // Method to retrieve all recommendations for a user
    List<RecommendationResponseDto> getRecommendations(UUID userId);
}
