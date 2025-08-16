package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.exceptions.UserArtistSaveException;
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


}
