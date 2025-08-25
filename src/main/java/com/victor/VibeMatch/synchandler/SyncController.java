package com.victor.VibeMatch.synchandler;

import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/sync")
public class SyncController {
    private final SyncOrchestrator syncOrchestrator;
    private final UserQueryService userQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('USER')")
    //Syncs for 60 seconds(or has a 60 second delay)
    public ResponseEntity<Map<String, String>> syncUserData(@AuthenticationPrincipal UserPrincipal userPrincipal){
        String spotifyId = userPrincipal.getSpotifyId();
        User user = userQueryService.findBySpotifyIdWithLock(spotifyId);
        String taskId = syncOrchestrator.scheduleUserSync(user);

        if (taskId == null) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", "User has synced within the last 12 hours or a sync is already in progress."));
        }

        return new ResponseEntity<>(Map.of("taskId", taskId), HttpStatus.ACCEPTED);
    }

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> getSyncStatus(@RequestParam String id){
        TaskStatus taskStatus = syncOrchestrator.getSyncStatus(id);
        return ResponseEntity.ok(Map.of("taskId", id, "status", taskStatus.getStatus()));

    }

    @GetMapping("/last-synced")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> getLastSyncedAt(@AuthenticationPrincipal UserPrincipal userPrincipal){
        boolean hasSyncedLast24Hours = syncOrchestrator.hasSyncedLast24Hours(userPrincipal.getId());
        return ResponseEntity.ok(Map.of("hasSynced", hasSyncedLast24Hours));

    }



}
