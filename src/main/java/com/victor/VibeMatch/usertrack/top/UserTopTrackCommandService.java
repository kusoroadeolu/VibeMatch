package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.user.User;

import java.util.List;

public interface UserTopTrackCommandService {
    List<UserTopTrack> saveTopTracks(List<UserTopTrack> tracks);

    void deleteAllTopTracksByUser(User user);
}
