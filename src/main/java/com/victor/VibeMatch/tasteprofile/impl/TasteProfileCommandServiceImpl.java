package com.victor.VibeMatch.tasteprofile.impl;

import com.victor.VibeMatch.exceptions.TasteProfileSaveException;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileCommandService;
import com.victor.VibeMatch.tasteprofile.TasteProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TasteProfileCommandServiceImpl implements TasteProfileCommandService {
    private final TasteProfileRepository tasteProfileRepository;

    @Override
    public TasteProfile saveTasteProfile(TasteProfile tasteProfile){
        try{
            log.info("Attempting to save taste profile for user: {}", tasteProfile.getUser().getUsername());
            var saved = tasteProfileRepository.save(tasteProfile);
            log.info("Successfully saved taste profile for user: {}", tasteProfile.getUser().getUsername());
            return saved;
        }catch (DataIntegrityViolationException e){
            log.error("A data integrity exception occurred while trying to save taste profile for user: {}", tasteProfile.getUser().getUsername());
            throw new TasteProfileSaveException(String.format("A data integrity exception occurred while trying to save taste profile for user: %s", tasteProfile.getUser().getUsername()));
        }catch (Exception e){
            log.error("An unexpected exception occurred while trying to save taste profile for user: {}", tasteProfile.getUser().getUsername());
            throw new TasteProfileSaveException(String.format("An unexpected exception occurred while trying to save taste profile for user: %s", tasteProfile.getUser().getUsername()));
        }
    }

}
