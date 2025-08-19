package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;

import java.util.List;

public interface UserArtistCommandService {
    List<UserArtist> saveUserArtists(List<UserArtist> userArtists);


    void deleteByUser(User user);
}
