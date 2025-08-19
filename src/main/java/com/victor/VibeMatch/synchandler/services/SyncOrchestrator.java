package com.victor.VibeMatch.synchandler.services;

import com.victor.VibeMatch.synchandler.TaskStatus;
import com.victor.VibeMatch.user.User;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

public interface SyncOrchestrator {
    @Transactional
    LocalDateTime syncAllData(User user);

    String scheduleUserSync(User user);

    TaskStatus getSyncStatus(String taskId);
}
