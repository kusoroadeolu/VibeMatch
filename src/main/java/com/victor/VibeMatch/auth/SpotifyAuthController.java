package com.victor.VibeMatch.auth;

import com.victor.VibeMatch.auth.dtos.LoginResponseDto;
import com.victor.VibeMatch.auth.dtos.SpotifyTokenResponse;
import com.victor.VibeMatch.auth.dtos.SpotifyUserProfile;
import com.victor.VibeMatch.auth.service.SpotifyAuthService;
import com.victor.VibeMatch.auth.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/login")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<Void> handleSpotifyLogin(){
        URI authUri = spotifyAuthService.buildAuthUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(authUri);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);

    }

    @GetMapping("/callback")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<LoginResponseDto> callback(@RequestParam String code){
        ResponseEntity<SpotifyTokenResponse> tokenResponse = spotifyAuthService.handleCallback(code);
        SpotifyTokenResponse tokenResponseBody = tokenResponse.getBody();

        SpotifyUserProfile userProfile = spotifyAuthService.getSpotifyUser("Bearer " + tokenResponseBody.getAccessToken());

        LoginResponseDto responseDto = userAuthService.loginUser(userProfile, tokenResponseBody);
        return ResponseEntity.ok(responseDto);
    }

}
