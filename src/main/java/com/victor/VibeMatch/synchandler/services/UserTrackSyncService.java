package com.victor.VibeMatch.synchandler.services;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;

import java.util.List;

public interface UserTrackSyncService {
    List<UserRecentTrack> syncRecentUserTracks(User user, String spotifyId);

    List<UserTopTrack> syncTopUserTracks(User user, String spotifyId);
}
