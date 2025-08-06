package com.victor.VibeMatch.user;

import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.auth.service.TokenRefreshService;
import com.victor.VibeMatch.auth.service.UserAuthServiceImpl;
import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.user.service.UserQueryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserQueryServiceImpl userQueryService;
    private final UserAuthServiceImpl userAuthService;
    private final TokenRefreshService tokenRefreshService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<UserResponseDto> getUserData(@AuthenticationPrincipal UserPrincipal userPrincipal){
        String spotifyId = userPrincipal.getSpotifyId();
        var response = userQueryService.getUserData(spotifyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TokenDto> refreshAccessToken(@AuthenticationPrincipal UserPrincipal userPrincipal){
        String spotifyId = userPrincipal.getSpotifyId();
        var token = tokenRefreshService.refreshUserAccessToken(spotifyId);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/access-token")
    @ResponseStatus(HttpStatus.FOUND)
    @PreAuthorize("hasRole('USER')")
    public String getAccessToken(@AuthenticationPrincipal UserPrincipal userPrincipal){
        log.info("Successfully hit the access token endpoint. User: {}", userPrincipal.getUsername());
        String spotifyId = userPrincipal.getSpotifyId();
        return userAuthService.getUserAccessToken(spotifyId);
    }

    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal){
        String spotifyId = userPrincipal.getSpotifyId();
        userAuthService.logoutUser(spotifyId);
        return ResponseEntity.ok().build();
    }
}
