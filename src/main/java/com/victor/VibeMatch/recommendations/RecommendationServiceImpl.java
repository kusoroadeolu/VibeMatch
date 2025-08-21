package com.victor.VibeMatch.recommendations;

import com.victor.VibeMatch.connections.ConnectionQueryService;
import com.victor.VibeMatch.exceptions.NoSuchConnectionException;
import com.victor.VibeMatch.recommendations.dtos.RecommendationRequestDto;
import com.victor.VibeMatch.recommendations.dtos.RecommendationResponseDto;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final RecommendationRepository recommendationRepository;
    private final ConnectionQueryService connectionQueryService;
    private final UserQueryService userQueryService;

    // Method to handle sending a new recommendation
    @Override
    public void sendRecommendation(UUID recommenderId, UUID receiverId, RecommendationRequestDto requestDto) {
        User recommender = userQueryService.findByUserId(recommenderId);
        User receiver = userQueryService.findByUserId(receiverId);

        //Ensure the users are connected before allowing a recommendation
        if (!connectionQueryService.activeConnectionExists(recommender, receiver)) {
            log.warn("Cannot send recommendation: {} and {} are not connected.", recommender.getUsername(), receiver.getUsername());
            throw new NoSuchConnectionException("Cannot send recommendation to a user you are not connected with.");
        }

        Recommendation recommendation = Recommendation.builder()
                .recommender(recommender)
                .recommendedTo(receiver)
                .spotifyUrl(requestDto.spotifyUrl())
                .recommendedName(requestDto.recommendedName())
                .type(requestDto.type())
                .build();

        recommendationRepository.save(recommendation);
        log.info("Recommendation sent from {} to {}.", recommender.getUsername(), receiver.getUsername());
    }

    // Method to retrieve all recommendations for a user
    @Override
    public List<RecommendationResponseDto> getRecommendations(UUID userId) {
        User user = userQueryService.findByUserId(userId);
        List<Recommendation> recommendations = recommendationRepository.findByRecommendedTo(user);

        log.info("Found {} recommendations for user: {}.", recommendations.size(), user.getUsername());

        return recommendations.stream()
                .map(rec -> new RecommendationResponseDto(
                        rec.getRecommender().getUsername(),
                        rec.getSpotifyUrl(),
                        rec.getRecommendedName(),
                        rec.getType(),
                        rec.getCreatedAt()
                ))
                .toList();
    }
}
