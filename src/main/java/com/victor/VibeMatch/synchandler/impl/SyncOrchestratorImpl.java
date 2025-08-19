package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.synchandler.TaskStatus;
import com.victor.VibeMatch.exceptions.UserSyncException;
import com.victor.VibeMatch.rabbitmq.RabbitSyncConfigProperties;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import com.victor.VibeMatch.synchandler.services.TaskService;
import com.victor.VibeMatch.synchandler.services.UserArtistSyncService;
import com.victor.VibeMatch.synchandler.services.UserTrackSyncService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.victor.VibeMatch.synchandler.TaskStatus.*;
import static java.util.concurrent.CompletableFuture.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class SyncOrchestratorImpl implements SyncOrchestrator {
    private final UserArtistSyncService userArtistSyncService;
    private final UserTrackSyncService userTrackSyncService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitSyncConfigProperties rabbitSyncConfigProperties;
    private final TaskService taskService;
    private final UserQueryService userQueryService;

    /**
     * Syncs all user top tracks and artists
     * @param user The user
     * @return The date time of when the sync completed
     * */
    @Transactional
    @Override
    public LocalDateTime syncAllData(User user){
        log.info("Initiating user data sync");

        CompletableFuture<Void> artistSync = runAsync(() -> userArtistSyncService.syncUserArtist(user));
        CompletableFuture<Void> recentTrackSync = runAsync(() -> userTrackSyncService.syncRecentUserTracks(user));
        CompletableFuture<Void> topTrackSync = runAsync(() -> userTrackSyncService.syncTopUserTracks(user));
        allOf(artistSync, recentTrackSync, topTrackSync).join();

        LocalDateTime syncedAt = LocalDateTime.now();
        log.info("All sync operations completed for user: {} at: {}", user.getUsername(), syncedAt);
        return syncedAt;
    }

    //Checks if a user has synced previously
    public boolean hasSyncedRecently(LocalDateTime now, User user){
        LocalDateTime lastSyncedAt = user.getLastSyncedAt();
        return lastSyncedAt != null && lastSyncedAt.isAfter(now.minusHours(12));
    }

    /**
     * Message producer which sends users to a queue to sync them
     * @param user The user being synced
     * */
    @Override
    public String scheduleUserSync(User user){
        LocalDateTime now = LocalDateTime.now();
        String spotifyId = user.getSpotifyId();

        TaskStatus status = taskService.getTaskStatus(spotifyId);

        if(status == PENDING){
            log.warn("User: {} sync is still in progress", user.getUsername());
            return null;
        }

        if(hasSyncedRecently(now, user)){
            log.warn("User: {} has synced within the last 12 hours.", user.getUsername());
            return null;
        }

        String taskId = UUID.randomUUID().toString();
        validateSpotifyId(spotifyId);

        log.info("Starting sync for user: {}", spotifyId);
        taskService.saveTask(taskId, PENDING);

         rabbitTemplate.convertAndSend(
                rabbitSyncConfigProperties.getExchangeName() + ".dlx",
                 rabbitSyncConfigProperties.getRoutingKey(),
                 spotifyId,
                 message -> {
                    message.getMessageProperties().getHeaders().put("taskId", taskId);
                    return message;
                 }
         );
        log.info("Sync message sent for user with Spotify ID: {}", spotifyId);
        return taskId;

    }

    @Override
    public TaskStatus getSyncStatus(String taskId){
        if(taskId == null || taskId.isEmpty()){
            log.error("Cannot return sync status for null or empty task ID");
            throw new UserSyncException("Cannot return sync status for null or empty task ID");
        }

        return taskService.getTaskStatus(taskId);
    }


    //Ensures a given spotify ID is not null or empty
    public void validateSpotifyId(String spotifyId){
        if(spotifyId == null || spotifyId.isEmpty()){
            log.error("Spotify ID cannot be null or empty");
            throw new RuntimeException("Spotify ID cannot be null or empty");
        }
    }







}
