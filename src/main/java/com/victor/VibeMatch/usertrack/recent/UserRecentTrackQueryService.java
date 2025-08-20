package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.user.User;

import java.util.List;

public interface UserRecentTrackQueryService {
    List<UserRecentTrack> findByUser(User user);

    boolean existsByUser(User user);
}
