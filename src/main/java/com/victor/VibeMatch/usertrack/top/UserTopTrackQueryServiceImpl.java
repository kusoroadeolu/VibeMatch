package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserTopTrackQueryServiceImpl implements UserTopTrackQueryService{
    private final UserTopTrackRepository userTopTrackRepository;

    @Override
    public List<UserTopTrack> findByUser(User user){
        return userTopTrackRepository.findByUser(user);
    }

    @Override
    public boolean existsByUser(User user){
        return userTopTrackRepository.existsByUser(user);
    }
}
