package com.victor.VibeMatch.jwt;

import com.victor.VibeMatch.exceptions.JwtException;
import com.victor.VibeMatch.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService{

    private final JwtConfigProperties configProperties;

    private SecretKey generateKey(){
        byte[] bytes = Decoders.BASE64URL.decode(configProperties.getSecret());
        return Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String generateToken(UserPrincipal userPrincipal){
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userPrincipal.getEmail());

        return Jwts
                .builder()
                .claims(claims)
                .subject(userPrincipal.getSpotifyId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (configProperties.getExpiration() * 1000L)))
                .signWith(generateKey())
                .compact();
    }

    private Claims extractAllClaims(String token){
        try{
            return Jwts
                    .parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception e){
            log.error("An error occurred while trying to extract claims from a jwt token", e);
            throw new JwtException("An error occurred while trying to extract claims from a jwt token", e);
        }
    }

    @Override
    public boolean validateToken(String token, UserPrincipal userPrincipal){
        final String spotifyId = extractSpotifyId(token);
        return spotifyId.equals(userPrincipal.getSpotifyId()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractAllClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    }

    @Override
    public String extractSpotifyId(String token){
        return extractAllClaims(token).getSubject();
    }


}
