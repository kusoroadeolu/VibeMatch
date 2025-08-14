package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.cache.TaskStatus;
import com.victor.VibeMatch.exceptions.UserSyncException;
import com.victor.VibeMatch.rabbitmq.RabbitSyncConfigProperties;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import com.victor.VibeMatch.synchandler.services.TaskService;
import com.victor.VibeMatch.synchandler.services.UserArtistSyncService;
import com.victor.VibeMatch.synchandler.services.UserTrackSyncService;
import com.victor.VibeMatch.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    @Transactional
    @Override
    public void syncAllData(String spotifyId, User user){
        validateSpotifyId(spotifyId);
        log.info("Initiating user data sync");
        CompletableFuture<Void> artistSync = runAsync(() -> userArtistSyncService.syncUserArtist(user, spotifyId));
        CompletableFuture<Void> recentTrackSync = runAsync(() -> userTrackSyncService.syncRecentUserTracks(user, spotifyId));
        CompletableFuture<Void> topTrackSync = runAsync(() -> userTrackSyncService.syncTopUserTracks(user, spotifyId));
        allOf(artistSync, recentTrackSync, topTrackSync).join();
        log.info("All sync operations completed for user: {}", user.getUsername());

    }

    /**
     * Message producer which sends users to a queue to sync them
     * @param spotifyId The spotify ID of the user
     * */
    @Override
    public String scheduleUserSync(String spotifyId){
        String taskId = UUID.randomUUID().toString();
        validateSpotifyId(spotifyId);

        log.info("Starting sync for user: {}", spotifyId);
        taskService.saveTask(taskId, TaskStatus.PENDING);

         rabbitTemplate.convertAndSend(
                rabbitSyncConfigProperties.getExchangeName() + ".dlx",
                 rabbitSyncConfigProperties.getRoutingKey(),
                 spotifyId,
                 message -> {
                    message.getMessageProperties().setHeader("taskId", taskId);
                    return message;
                 }
         );
        log.info("Sync message sent for user: {}", spotifyId);
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
