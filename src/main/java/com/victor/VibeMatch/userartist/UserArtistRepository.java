package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserArtistRepository extends JpaRepository<UserArtist, UUID> {
    List<UserArtist> findByUser(User user);

    List<UserArtist> findByUserOrderByRankingAsc(User user, Limit limit);

    void deleteByUser(User user);
}
