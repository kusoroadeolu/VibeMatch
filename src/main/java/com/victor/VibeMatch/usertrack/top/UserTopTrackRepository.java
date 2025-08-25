package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserTopTrackRepository extends JpaRepository<UserTopTrack, UUID> {

    List<UserTopTrack> findByUser(User user);

    @Modifying
    @Query("DELETE FROM UserTopTrack ut WHERE ut.user = :user")
    void deleteByUser(@Param("user") User user);

    boolean existsByUser(User user);
}
