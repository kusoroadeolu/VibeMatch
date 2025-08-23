package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRecentTrackRepository extends JpaRepository<UserRecentTrack, UUID> {

    List<UserRecentTrack> findByUser(User user);

    void deleteByUser(User user);

    boolean existsByUser(User user);
}
