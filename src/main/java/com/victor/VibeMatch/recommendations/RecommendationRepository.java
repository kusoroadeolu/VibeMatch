package com.victor.VibeMatch.recommendations;

import com.victor.VibeMatch.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findByRecommendedTo(User recommendedTo);
}
