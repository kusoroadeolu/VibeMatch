package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;

public interface ConnectionCommandService {
    Connection saveConnection(Connection connection);

    void deleteConnection(User userA, User userB);

}
