package com.victor.VibeMatch.tasteprofile.impl;

import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileCalculationService;
import com.victor.VibeMatch.tasteprofile.TasteProfileCommandService;
import com.victor.VibeMatch.tasteprofile.TasteProfilePersistenceService;
import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TasteProfilePersistenceServiceImpl implements TasteProfilePersistenceService {
    private final TasteProfileCalculationService tasteProfileCalculationService;
    private final TasteProfileCommandService tasteProfileCommandService;
    private final UserQueryService userQueryService;

    @Override
    public TasteProfile createUserTasteProfile(UUID userId){
        User user = userQueryService.findByUserId(userId);


        TasteProfile tasteProfile = user.getTasteProfile();

        if(tasteProfile == null){
             tasteProfile = buildTasteProfile(
                    user,
                    tasteProfileCalculationService.calculateTopGenres(user),
                    tasteProfileCalculationService.calculateTopArtists(user),
                    tasteProfileCalculationService.calculateMainStreamScore(user),
                    tasteProfileCalculationService.calculateDiscoveryPattern(user)
            );
        }else{
           updateExistingTasteProfile(tasteProfile, user);
        }

        TasteProfile saved = tasteProfileCommandService.saveTasteProfile(tasteProfile);
        log.info("Successfully created taste profile for user: {}", user.getUsername());
        return saved;
    }

    private void updateExistingTasteProfile(TasteProfile existingProfile, User user) {
        existingProfile.setTopGenres(tasteProfileCalculationService.calculateTopGenres(user));
        existingProfile.setTopArtists(tasteProfileCalculationService.calculateTopArtists(user));
        existingProfile.setMainstreamScore(tasteProfileCalculationService.calculateMainStreamScore(user));
        existingProfile.setDiscoveryPattern(tasteProfileCalculationService.calculateDiscoveryPattern(user));
    }


    @Override
    public TasteProfile buildTasteProfile(User user, List<TasteWrapper> genreWrapper, List<TasteWrapper> artistWrapper, double mainstreamScore, double discoveryPattern){
        return TasteProfile
                .builder()
                .user(user)
                .discoveryPattern(discoveryPattern)
                .mainstreamScore(mainstreamScore)
                .topArtists(artistWrapper)
                .topGenres(genreWrapper)
                .build();
    }
}
