package com.victor.VibeMatch.tasteprofile.impl;

import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.TasteProfileCalculationService;
import com.victor.VibeMatch.tasteprofile.TasteProfileCommandService;
import com.victor.VibeMatch.tasteprofile.TasteProfileCreationService;
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
public class TasteProfileCreationServiceImpl implements TasteProfileCreationService {
    private final TasteProfileCalculationService tasteProfileCalculationService;
    private final TasteProfileCommandService tasteProfileCommandService;
    private final UserQueryService userQueryService;

    @Override
    public TasteProfile createUserTasteProfile(UUID userId){
        User user = userQueryService.findByUserId(userId);
        TasteProfile tasteProfile = buildTasteProfile(
                user,
                tasteProfileCalculationService.calculateTopGenres(user),
                tasteProfileCalculationService.calculateTopArtists(user),
                tasteProfileCalculationService.calculateMainStreamScore(user),
                tasteProfileCalculationService.calculateDiscoveryPattern(user)
        );

        TasteProfile saved = tasteProfileCommandService.saveTasteProfile(tasteProfile);
        log.info("Successfully created taste profile for user: {}", user.getUsername());
        return saved;
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
