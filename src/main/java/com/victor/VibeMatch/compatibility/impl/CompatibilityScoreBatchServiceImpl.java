package com.victor.VibeMatch.compatibility.impl;

import com.victor.VibeMatch.compatibility.CompatibilityScorePersistenceService;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.connections.Connection;
import com.victor.VibeMatch.connections.ConnectionQueryService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompatibilityScoreBatchServiceImpl implements com.victor.VibeMatch.compatibility.CompatibilityScoreBatchService {

    private final UserQueryService userQueryService;
    private final CompatibilityScorePersistenceService compatibilityScorePersistenceService;
    private final ConnectionQueryService connectionQueryService;

    /**
     * Attempts to find a list of compatible users for a user
     * @param user The user for which we are finding the compatible users
     * @return A list of dtos with compatible users
     * */
    @Override
    public List<CompatibilityScoreResponseDto> returnAllCompatibleUsers(User user, List<User> targetUsers){
        //Clear all previous compatibility scores before recalculating
        compatibilityScorePersistenceService.deleteCompatibilityScoresByUser(user);

        List<CompatibilityScoreResponseDto> compatibilityScores = new ArrayList<>();

        //Get the active connections for a user
        List<Connection> connections = connectionQueryService.findAllActiveConnections(user);
        Set<UUID> uuids = connections
                .stream()
                .flatMap(connection -> Stream.of(
                        connection.getRequester().getId(),
                        connection.getReceiver().getId()
                ))
                .collect(Collectors.toSet());


        for(User targetUser: targetUsers){
            UUID targetUserId = targetUser.getId();
            log.info("Current target user: {}", targetUser.getUsername());

            //If the users are already connected, skip
            if(uuids.contains(targetUserId))continue;

            if(!targetUser.isPublic() || targetUserId.equals(user.getId()) || targetUser.getTasteProfile() == null){
                continue;
            }

            CompatibilityScoreResponseDto score =
                    compatibilityScorePersistenceService.saveCompatibilityScore(user, targetUser);
//            if(score == null){
//                continue;
//            }
            compatibilityScores.add(score);


        }

        log.info("Found {} compatible users for user with ID: {}", compatibilityScores.size(), user.getId());
        compatibilityScores.sort(Comparator.comparing(CompatibilityScoreResponseDto::tasteCompatibilityScore)
                .thenComparing(CompatibilityScoreResponseDto::discoveryCompatibilityScore));
        return compatibilityScores;
    }

    @Override
    public List<CompatibilityScoreResponseDto> findCompatibleUsersInBatch(UUID userId){
        User user = userQueryService.findByUserId(userId);
        List<User> targetUsers = userQueryService.findAllPublicUsers();
        return returnAllCompatibleUsers(user, targetUsers);
    }







}
