package com.victor.VibeMatch.synchandler.impl;

import com.victor.VibeMatch.synchandler.TaskStatus;
import com.victor.VibeMatch.exceptions.SpotifyRateLimitException;
import com.victor.VibeMatch.exceptions.UserSyncException;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import com.victor.VibeMatch.synchandler.services.TaskService;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserCommandService;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncListener {


    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final TaskService taskService;
    private final RabbitTemplate rabbitTemplate;
    private final SyncOrchestrator syncOrchestrator;

    /**
     * Listener which process messages from the main sync queue
     * @param message The full message object from rabbit mq
     * */
    @RabbitListener(queues = "sync-queue")
    public void queueUsers(Message message) throws IOException {
        log.info("Sync Queue Hit");
        String spotifyId = new String(message.getBody());

        String taskId = (String) message
                .getMessageProperties()
                .getHeader("taskId");

        User user = userQueryService.findBySpotifyId(spotifyId);

        try{
            LocalDateTime syncedAt = syncOrchestrator.syncAllData(user);
            user.setLastSyncedAt(syncedAt);
            userCommandService.saveUser(user);
            taskService.saveTask(taskId, TaskStatus.SUCCESS);

            log.info("Successfully processed and acknowledged sync for user: {}", user.getUsername());

        }catch (SpotifyRateLimitException e){
            handleSpotifyRateLimitException(message, e, spotifyId, taskId);
            throw new AmqpRejectAndDontRequeueException("Rate limit exceeded, message re-sent to DLX");
        } catch (Exception e) {
            log.error("An error occurred during sync for user: {}. Rejecting message.", spotifyId, e);
            taskService.saveTask(taskId, TaskStatus.FAIL);
            throw new AmqpRejectAndDontRequeueException("Processing failed due to an unexpected error", e);
        }

    }

    public void handleSpotifyRateLimitException(Message message, SpotifyRateLimitException e, String spotifyId, String taskId){
        long retryDelaySeconds = e.getRetryAfterSeconds();
        log.error("Spotify rate limit threshold exceeded. Retrying after {} seconds", retryDelaySeconds);

        taskService.saveTask(taskId, TaskStatus.RETRYING);

        //Reroute to the dlx queue increasing its ttl by the number of seconds left before we can retry
        rabbitTemplate.convertAndSend(
                "sync-exchange.dlx",
                message.getMessageProperties().getReceivedRoutingKey(),
                spotifyId,
                msg -> {
                    msg.getMessageProperties().setExpiration(String.valueOf(retryDelaySeconds * 1000L));
                    return msg;
                }
        );
    }

}
