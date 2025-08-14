package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.user.User;

import java.util.List;

public interface UserRecentTrackCommandService {
    List<UserRecentTrack> saveRecentTracks(List<UserRecentTrack> tracks);

    void deleteAllRecentTracksByUser(User user);
}
