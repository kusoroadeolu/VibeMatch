package com.victor.VibeMatch.recommendations;

import com.victor.VibeMatch.misc.UUIDValidator;
import com.victor.VibeMatch.recommendations.dtos.RecommendationRequestDto;
import com.victor.VibeMatch.recommendations.dtos.RecommendationResponseDto;
import com.victor.VibeMatch.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UUIDValidator uuidValidator;

    @PostMapping("/{friendId}")
    public ResponseEntity<Void> sendRecommendation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String friendIdString,
            @RequestBody RecommendationRequestDto requestDto) {

        UUID friendId = uuidValidator.handleUUID(friendIdString);
        recommendationService.sendRecommendation(userPrincipal.getId(), friendId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendations(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<RecommendationResponseDto> recommendations = recommendationService.getRecommendations(userPrincipal.getId());
        return ResponseEntity.ok(recommendations);
    }
}