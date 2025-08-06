package com.victor.VibeMatch.security;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserQueryServiceImpl userQueryService;

    @Override
    public UserDetails loadUserByUsername(String username){
            User user = userQueryService.findByUsername(username);
            log.info("Found user: {} in the DB.", username);
            return new UserPrincipal(user);
    }

    @Override
    public UserPrincipal loadUserBySpotifyId(String spotifyId){
        User user = userQueryService.findBySpotifyId(spotifyId);
        log.info("Found user: {} in the DB with spotify ID: {}", user.getUsername(), spotifyId);
        return new UserPrincipal(user);
    }
}
