package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compatibility")
@Slf4j
public class CompatibilityScoreController {

    private final CompatibilityScorePersistenceService compatibilityScorePersistenceService;
    private final CompatibilityScoreBatchService compatibilityScoreBatchService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CompatibilityScoreResponseDto> calculateScore(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("id")UUID targetUserId){
        log.info("Target User ID: {}", targetUserId);
        UUID userId = userPrincipal.getId();
        var response = compatibilityScorePersistenceService.getCompatibilityScore(userId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/discover")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CompatibilityScoreResponseDto>> calculateScore(@AuthenticationPrincipal UserPrincipal userPrincipal){
        UUID userId = userPrincipal.getId();
        var response = compatibilityScoreBatchService.findCompatibleUsersInBatch(userId);
        return ResponseEntity.ok(response);
    }
}
