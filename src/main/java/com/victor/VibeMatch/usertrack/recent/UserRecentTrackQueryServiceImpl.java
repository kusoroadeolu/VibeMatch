package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRecentTrackQueryServiceImpl implements UserRecentTrackQueryService{

    private final UserRecentTrackRepository userRecentTrackRepository;

    @Override
    public List<UserRecentTrack> findByUser(User user){
        List<UserRecentTrack> recentTracks = userRecentTrackRepository.findByUser(user);
        log.info("Found {} recent tracks belonging to user: {}", recentTracks.size(), user.getUsername());
        return recentTracks;
    }

}
