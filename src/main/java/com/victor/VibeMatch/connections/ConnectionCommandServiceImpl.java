package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.exceptions.ConnectionPersistenceException;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionCommandServiceImpl implements ConnectionCommandService {

    private final ConnectionRepository connectionRepository;

    @Override
    public Connection saveConnection(Connection connection){
        try{
            log.info("Attempting to save connection.");
            var saved = connectionRepository.save(connection);
            log.info("Successfully saved connection.");
            return saved;
        }catch (DataIntegrityViolationException e){
            log.error("An data integrity error occurred while trying to save a connection", e);
            throw new ConnectionPersistenceException("An data integrity error occurred while trying to save a connection", e);
        }catch (Exception e){
            log.error("An unexpected error occurred while trying to save a connection", e);
            throw new ConnectionPersistenceException("An unexpected error occurred while trying to save a connection", e);
        }
    }

    @Override
    public void deleteConnection(User userA, User userB){

        User canonicalUserA = userA;
        User canonicalUserB = userB;

        if(userA.getId().compareTo(userB.getId()) < 0){
            canonicalUserA = userB;
            canonicalUserB = userA;
        }

        try{
            log.info("Attempting to deleted connection.");
            connectionRepository.deleteByUserAAndUserB(canonicalUserA, canonicalUserB);
            log.info("Successfully deleted connection.");
        }catch (DataIntegrityViolationException e){
            log.error("An data integrity error occurred while trying to delete a connection", e);
            throw new ConnectionPersistenceException("An data integrity error occurred while trying to delete a connection", e);
        }catch (Exception e){
            log.error("An unexpected error occurred while trying to delete a connection", e);
            throw new ConnectionPersistenceException("An unexpected error occurred while trying to delete a connection", e);
        }
    }

}
