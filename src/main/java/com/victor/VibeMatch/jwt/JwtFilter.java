package com.victor.VibeMatch.jwt;

import com.victor.VibeMatch.security.CustomUserDetailsService;
import com.victor.VibeMatch.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtServiceImpl jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String spotifyId = null;
        String token = null;

        Cookie jwtCookie = WebUtils.getCookie(request, "jwtToken");
        if (jwtCookie != null) {
            token = jwtCookie.getValue();
        }

        // Log the state of the SecurityContext before processing the token
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth == null) {
            log.info("SecurityContext is empty for URI: {}", request.getRequestURI());
        } else {
            log.info("SecurityContext has existing authentication: {} for URI: {}", currentAuth.getName(), request.getRequestURI());
        }


        if (token != null) {
            try {
                spotifyId = jwtService.extractSpotifyId(token);
                log.info("Successfully extracted Spotify ID: {}", spotifyId);
            } catch (Exception e) {
                log.warn("Failed to extract the Spotify ID from JWT Token. Message: {}", e.getMessage());
            }
        }

        // Only set authentication if the Spotify ID was successfully extracted
        if (spotifyId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserPrincipal userPrincipal = customUserDetailsService.loadUserBySpotifyId(spotifyId);

            if (userPrincipal != null && jwtService.validateToken(token, userPrincipal)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("Successfully authenticated user with Spotify ID: {}", spotifyId);
            }
        }

        filterChain.doFilter(request, response);
    }
}