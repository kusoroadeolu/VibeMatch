package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserTopTrackRepository extends JpaRepository<UserTopTrack, UUID> {

    List<UserTopTrack> findByUser(User user);

    int deleteByUser(User user);
}
