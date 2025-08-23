package com.victor.VibeMatch.synchandler.services;

import com.victor.VibeMatch.synchandler.TaskStatus;
import com.victor.VibeMatch.user.User;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SyncOrchestrator {
    @Transactional
    LocalDateTime syncAllData(User user);

    //Checks if a user has synced previously
    boolean hasSyncedRecently(LocalDateTime now, User user);

    boolean hasSyncedLast24Hours(UUID userId);

    String scheduleUserSync(User user);

    TaskStatus getSyncStatus(String taskId);
}
