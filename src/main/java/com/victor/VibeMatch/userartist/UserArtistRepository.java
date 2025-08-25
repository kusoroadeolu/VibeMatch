package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserArtistRepository extends JpaRepository<UserArtist, UUID> {
    List<UserArtist> findByUser(User user);

    List<UserArtist> findByUserOrderByRankingAsc(User user, Limit limit);

    @Modifying
    @Query("DELETE FROM UserArtist ua WHERE ua.user = :user")
    void deleteByUser(@Param("user") User user);

    boolean existsByUser(User user);
}
