package com.victor.VibeMatch.auth;

import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.auth.service.TokenRefreshService;
import com.victor.VibeMatch.auth.service.UserAuthServiceImpl;
import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.user.UserResponseDto;
import com.victor.VibeMatch.user.service.UserQueryServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAuthController {

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
    public ResponseEntity<String> getAccessToken(@AuthenticationPrincipal UserPrincipal userPrincipal){
        log.info("Successfully hit the access token endpoint. User: {}", userPrincipal.getUsername());
        String spotifyId = userPrincipal.getSpotifyId();
        String accessToken = userAuthService.getUserAccessToken(spotifyId);
        return new ResponseEntity<>(accessToken, HttpStatus.FOUND);
    }

    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal, HttpServletRequest request){
        String spotifyId = userPrincipal.getSpotifyId();
        userAuthService.logoutUser(spotifyId);
        return ResponseEntity.ok().build();
    }
}
