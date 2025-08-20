package com.victor.VibeMatch.usertrack.top;

import com.victor.VibeMatch.exceptions.NoSuchUserException;
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
@RequiredArgsConstructor
@Slf4j
public class UserTopTrackCommandServiceImpl implements UserTopTrackCommandService {

    private final UserTopTrackRepository userTopTrackRepository;

    @Override
    public List<UserTopTrack> saveTopTracks(List<UserTopTrack> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            log.debug("No tracks provided to save");
            return Collections.emptyList();
        }

        try {
            log.info("Saving {} top tracks", tracks.size());
            var saved = userTopTrackRepository.saveAll(tracks);
            log.debug("Successfully saved {} top tracks", tracks.size());
            return saved;

        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation while saving top tracks: {}", e.getMessage());
            throw new UserTrackSaveException("Invalid track data provided", e);

        } catch (Exception e) {
            log.error("Unexpected error saving top tracks", e);
            throw new UserTrackSaveException("Failed to save top tracks", e);
        }
    }

    @Override
    public void deleteAllTopTracksByUser(User user) {
        if (user == null) {
            log.warn("Attempted to delete tracks with null user");
            throw new UserTrackDeletionException("User cannot be null");
        }


        try {
            log.info("Deleting all top tracks for user: {}", user.getId());
            userTopTrackRepository.deleteByUser(user);

        } catch (Exception e) {
            log.error("Error deleting top tracks for user: {}", user.getId(), e);
            throw new UserTrackDeletionException("Failed to delete recent tracks for user: " + user.getId(), e);
        }
    }
}
