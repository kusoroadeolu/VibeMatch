package com.victor.VibeMatch.synchandler.services;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;

import java.util.List;

public interface UserArtistSyncService {
    public List<UserArtist> syncUserArtist(User user, String spotifyId);
}
