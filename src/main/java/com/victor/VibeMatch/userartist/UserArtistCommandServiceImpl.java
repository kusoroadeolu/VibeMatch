package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.exceptions.UserArtistDeletionException;
import com.victor.VibeMatch.exceptions.UserArtistSaveException;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserArtistCommandServiceImpl implements UserArtistCommandService {

    private final UserArtistRepository userArtistRepository;

    @Override
    public List<UserArtist> saveUserArtists(List<UserArtist> userArtists){
        int size = userArtists.size();

        try{
            log.info("Attempting to save {} user artists.", size);
            var saved = userArtistRepository.saveAll(userArtists);
            log.info("Successfully saved {} user artists", size);
            return saved;
        }catch (DataIntegrityViolationException e){
            log.error("A data integrity error occurred while trying to save user artists. Size: {}", size);
            throw new UserArtistSaveException(String.format("A data integrity error occurred while trying to save user artists. Size: %s", size));
        }catch (Exception e){
            log.error("An unexpected error occurred while trying to save user artists. Size: {}", size);
            throw new UserArtistSaveException(String.format("An unexpected error occurred while trying to save user artists. Size: %s", size));

        }
    }

    @Override
    public void deleteByUser(User user){

        if(user == null){
            log.info("User cannot be null");
            throw new UserArtistDeletionException("User cannot be null");
        }

        try{
            log.info("Attempting to delete all user artists for user: {}", user.getUsername());
            userArtistRepository.deleteByUser(user);
            log.info("Successfully deleted all user artists for user: {}", user.getUsername());
        }catch (Exception e){
            log.info("An error occurred while trying to delete user artists for user: {}", user.getUsername());
            throw new UserArtistDeletionException(String.format("An error occurred while trying to delete user artists for user: %s", user.getUsername()));
        }
    }




}
