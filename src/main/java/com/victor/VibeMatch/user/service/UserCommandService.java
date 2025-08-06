package com.victor.VibeMatch.user.service;

import com.victor.VibeMatch.user.User;

public interface UserCommandService {

    User saveUser(User user);

    void updateRefreshToken(User user, String refreshToken);
}
