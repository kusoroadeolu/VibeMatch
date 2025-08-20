package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.user.User;

import java.util.List;

public interface UserTopTrackQueryService {
    List<UserTopTrack> findByUser(User user);

    boolean existsByUser(User user);
}
