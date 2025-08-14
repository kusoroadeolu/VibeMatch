package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.exceptions.UserTrackDeletionException;
import com.victor.VibeMatch.exceptions.UserTrackSaveException;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRecentTrackCommandServiceImpl implements UserRecentTrackCommandService{

    private final UserRecentTrackRepository userRecentTrackRepository;

    @Override
    public List<UserRecentTrack> saveRecentTracks(List<UserRecentTrack> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            log.debug("No tracks provided to save");
            return Collections.emptyList();
        }

        try {
            log.info("Saving {} recent tracks", tracks.size());
            var saved = userRecentTrackRepository.saveAll(tracks);
            log.debug("Successfully saved {} recent tracks", tracks.size());
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation while saving recent tracks: {}", e.getMessage());
            throw new UserTrackSaveException("Invalid track data provided", e);

        } catch (Exception e) {
            log.error("Unexpected error saving recent tracks", e);
            throw new UserTrackSaveException("Failed to save recent tracks", e);
        }
    }

    @Override
    public void deleteAllRecentTracksByUser(User user) {
        if (user == null) {
            log.warn("Attempted to delete tracks with null user");
            throw new UserTrackDeletionException("User cannot be null");
        }


        try {
            log.info("Deleting all recent tracks for user: {}", user.getId());
            int deletedCount = userRecentTrackRepository.deleteByUser(user);

            if (deletedCount > 0) {
                log.debug("Deleted {} recent tracks for user: {}", deletedCount, user.getId());
            } else {
                log.debug("No recent tracks found for user: {}", user.getId());
            }

        } catch (Exception e) {
            log.error("Error deleting recent tracks for user: {}", user.getId(), e);
            throw new UserTrackDeletionException("Failed to delete recent tracks for user: " + user.getId(), e);
        }
    }


}