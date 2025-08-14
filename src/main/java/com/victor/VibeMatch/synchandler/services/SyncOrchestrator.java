package com.victor.VibeMatch.synchandler.services;

import com.victor.VibeMatch.cache.TaskStatus;
import com.victor.VibeMatch.user.User;
import jakarta.transaction.Transactional;

public interface SyncOrchestrator {
    @Transactional
    void syncAllData(String spotifyId, User user);

    String scheduleUserSync(String spotifyId);

    TaskStatus getSyncStatus(String taskId);
}
