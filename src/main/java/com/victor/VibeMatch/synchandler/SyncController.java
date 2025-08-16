package com.victor.VibeMatch.synchandler;

import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/sync")
public class SyncController {
    private final SyncOrchestrator syncOrchestrator;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> syncUserData(@AuthenticationPrincipal UserPrincipal userPrincipal){
        String spotifyId = userPrincipal.getSpotifyId();
        log.info("Spotify ID: {}", spotifyId);
        String taskId = syncOrchestrator.scheduleUserSync(spotifyId);
        return new ResponseEntity<>(taskId, HttpStatus.ACCEPTED);
    }

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> getSyncStatus(@RequestParam String id){
        TaskStatus taskStatus = syncOrchestrator.getSyncStatus(id);
        return ResponseEntity.ok(taskStatus.getStatus());
    }



}
