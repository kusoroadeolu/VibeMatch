package com.victor.VibeMatch.jwt;

import com.victor.VibeMatch.exceptions.JwtException;
import com.victor.VibeMatch.security.CustomUserDetailsService;
import com.victor.VibeMatch.security.CustomUserDetailsServiceImpl;
import com.victor.VibeMatch.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtServiceImpl jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String spotifyId = null;
        String token = null;

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);

        try{
            spotifyId = jwtService.extractSpotifyId(token);
        }catch (Exception e){
            log.warn("Failed to extract the Spotify ID from JWT Token. Token may be invalid or expired. Message: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }



        if(spotifyId != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserPrincipal userPrincipal = customUserDetailsService.loadUserBySpotifyId(spotifyId);

            if(jwtService.validateToken(token, userPrincipal)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("Successfully authenticated user with Spotify ID: {}", spotifyId);
            }
        }


        filterChain.doFilter(request, response);
    }
}
