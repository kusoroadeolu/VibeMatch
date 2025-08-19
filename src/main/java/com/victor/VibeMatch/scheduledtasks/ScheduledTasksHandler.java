package com.victor.VibeMatch.scheduledtasks;

import com.victor.VibeMatch.compatibility.CompatibilityScoreBatchService;
import com.victor.VibeMatch.exceptions.ScheduledTaskException;
import com.victor.VibeMatch.synchandler.services.SyncOrchestrator;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksHandler {

    private final UserQueryService userQueryService;
    private final CompatibilityScoreBatchService compatibilityScoreBatchService;
    private final SyncOrchestrator syncOrchestrator;

    @Value("${scheduled.threshold-in-hours}")
    private int thresholdInHours;

    //Checks every 2 hours to sync users who haven't synced their data in 24 hours
    @Scheduled(fixedRateString = "${scheduled.refresh-user-data}")
    public void refreshUserData(){
        LocalDateTime threshold = LocalDateTime.now().minusHours(thresholdInHours);

        List<User> users = userQueryService.findByLastSyncedAtBefore(threshold);

        if(users.isEmpty()){
            log.info("Found no users to sync");
            return;
        }

        log.info("Found: {} users that haven't synced in {} hours", users.size(), thresholdInHours);



        for(User user: users){
            try{
                syncOrchestrator.scheduleUserSync(user);
            }catch (Exception e){
                log.info("An unexpected exception occurred while trying to re-sync {} user data", users.size(), e);
                throw new ScheduledTaskException(String.format("An unexpected exception occurred while trying to re-sync %s user data", users.size()), e);
            }
        }

        log.info("Successfully scheduled sync for {} users", users.size());
    }

    @Scheduled(cron = "${scheduled.compatibility-cron}")
    public void recalculateCompatibilityScores(){
        List<User> users = userQueryService.findAllUsers();

        if(users.isEmpty()){
            log.info("Found no users to calculate compatibility scores for");
            return;
        }

        log.info("Batch recalculating compatibility scores for: {} users", users.size());

        for(User user: users){
            try{
                compatibilityScoreBatchService.returnAllCompatibleUsers(user, users);
            }catch (Exception e){
                log.info("An unexpected exception occurred while trying to recalculate all compatibility scores for {} users", users.size(), e);
                throw new ScheduledTaskException(String.format("An unexpected exception occurred while trying to recalculate all compatibility scores for %s users", users.size()), e);
            }
        }

        log.info("Successfully recalculated compatibility scores for all users");
    }

}
