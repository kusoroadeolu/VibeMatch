package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserArtistQueryServiceImpl implements UserArtistQueryService{

    private final UserArtistRepository userArtistRepository;

    @Override
    public List<UserArtist> findArtistsByUser(User user){
        log.info("Attempting to find artists for user: {}", user.getUsername());
        List<UserArtist> artists = userArtistRepository.findByUser(user);
        log.info("Successfully found: {} artists for user: {}", artists.size(), user.getUsername());
        return artists;
    }

    @Override
    public List<UserArtist> findArtistsByUserOrderByRanking(User user, int limit){
        Limit limit1 = Limit.of(limit);
        log.info("Attempting to find top {} artists for user: {}", limit ,user.getUsername());
        List<UserArtist> artists = userArtistRepository.findByUserOrderByRankingAsc(user, limit1);
        log.info("Successfully found top : {} artists for user: {}", artists.size(), user.getUsername());
        return artists;
    }

    @Override
    public boolean existsByUser(User user){
        return userArtistRepository.existsByUser(user);
    }

}
