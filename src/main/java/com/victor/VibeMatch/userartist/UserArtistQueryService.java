package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;

import java.util.List;

public interface UserArtistQueryService {
    List<UserArtist> findArtistsByUser(User user);

    List<UserArtist> findArtistsByUserOrderByRanking(User user, int limit);
}
