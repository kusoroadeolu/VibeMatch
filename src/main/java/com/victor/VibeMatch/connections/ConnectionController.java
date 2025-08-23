package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.misc.UUIDValidator;
import com.victor.VibeMatch.security.UserPrincipal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UUIDValidator uuidValidator;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ActiveConnectionResponseDto>> getAllActiveConnectionsForAUser(@AuthenticationPrincipal UserPrincipal userPrincipal){
        UUID userId = userPrincipal.getId();
        List<ActiveConnectionResponseDto> dtos = connectionService.findAllActiveConnections(userId);
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InactiveConnectionResponseDto>> getPendingReceivedConnections(@AuthenticationPrincipal UserPrincipal userPrincipal){
        UUID userId = userPrincipal.getId();
        List<InactiveConnectionResponseDto> dtos = connectionService.findPendingReceivedConnections(userId);
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/sent")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InactiveConnectionResponseDto>> getPendingSentConnections(@AuthenticationPrincipal UserPrincipal userPrincipal){
        UUID userId = userPrincipal.getId();
        List<InactiveConnectionResponseDto> dtos = connectionService.findPendingSentConnections(userId);
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActiveConnectionResponseDto> getActiveConnection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                           @PathVariable("userId") String targetUserIdString){
        UUID id = userPrincipal.getId();
        UUID targetUserId = uuidValidator.handleUUID(targetUserIdString);

        ActiveConnectionResponseDto dto = connectionService.findActiveConnectionBetweenTwoUsers(id, targetUserId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/request/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ConnectionWrapperResponseDto> sendConnectionRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("userId") String targetUserIdString){
        UUID id = userPrincipal.getId();
        UUID targetUserId = uuidValidator.handleUUID(targetUserIdString);
        ConnectionWrapperResponseDto dto = connectionService.requestConnection(id, targetUserId);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeConnection(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("userId") String targetUserIdString){
        UUID id = userPrincipal.getId();
        UUID targetUserId = uuidValidator.handleUUID(targetUserIdString);
        connectionService.removeConnection(id, targetUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{userId}/accept")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActiveConnectionResponseDto> acceptConnectionRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("userId") String targetUserIdString){
        UUID receiverId = userPrincipal.getId();
        UUID targetUserId = uuidValidator.handleUUID(targetUserIdString);
        ActiveConnectionResponseDto dto = connectionService.acceptConnection(targetUserId, receiverId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }




}
