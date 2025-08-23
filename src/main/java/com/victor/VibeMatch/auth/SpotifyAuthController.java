package com.victor.VibeMatch.auth;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import com.victor.VibeMatch.auth.service.SpotifyAuthService;
import com.victor.VibeMatch.auth.service.UserAuthService;
import com.victor.VibeMatch.jwt.JwtConfigProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class SpotifyAuthController {
    private final SpotifyRegistrationConfigProperties configProperties;
    private final SpotifyAuthService spotifyAuthService;
    private final UserAuthService userAuthService;
    private final JwtConfigProperties jwtConfigProperties;

    @GetMapping("/login")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<Void> handleSpotifyLogin(){
        URI authUri = spotifyAuthService.buildAuthUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(authUri);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);

    }

    @GetMapping("/api/callback")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<LoginResponseDto> callback(@RequestParam String code){
        ResponseEntity<SpotifyTokenResponse> tokenResponse = spotifyAuthService.handleCallback(code);
        SpotifyTokenResponse tokenResponseBody = tokenResponse.getBody();

        SpotifyUserProfile userProfile = spotifyAuthService.getSpotifyUser("Bearer " + tokenResponseBody.getAccessToken());

        LoginResponseDto responseDto = userAuthService.loginUser(userProfile, tokenResponseBody);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam String code, HttpServletResponse response) {
        ResponseEntity<SpotifyTokenResponse> tokenResponse = spotifyAuthService.handleCallback(code);
        SpotifyTokenResponse tokenResponseBody = tokenResponse.getBody();
        SpotifyUserProfile userProfile = null;

        if(tokenResponseBody != null){
             userProfile = spotifyAuthService.getSpotifyUser("Bearer " + tokenResponseBody.getAccessToken());
        }

        LoginResponseDto responseDto = userAuthService.loginUser(userProfile, tokenResponseBody);

        // 1. Create HTTP-only cookies for tokens
        Cookie jwtCookie = new Cookie("jwtToken", responseDto.jwtToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtConfigProperties.getExpiration() * 1000);

        Cookie refreshCookie = new Cookie("refreshToken", responseDto.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // Set to true for production (HTTPS)
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 3600);

        // 2. Add cookies to the HTTP response
        response.addCookie(jwtCookie);
        response.addCookie(refreshCookie);

        // 3. Redirect to the dashboard.html without any query parameters
        return new RedirectView("/dashboard.html");
    }

}
