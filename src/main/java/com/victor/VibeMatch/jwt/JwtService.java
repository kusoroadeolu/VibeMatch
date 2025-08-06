package com.victor.VibeMatch.jwt;

import com.victor.VibeMatch.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(UserPrincipal userPrincipal);

    boolean validateToken(String token, UserPrincipal userPrincipal);

    String extractSpotifyId(String token);
}
