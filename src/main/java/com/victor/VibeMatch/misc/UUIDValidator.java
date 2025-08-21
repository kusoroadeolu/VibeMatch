package com.victor.VibeMatch.misc;

import com.victor.VibeMatch.exceptions.InvalidUUIDException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class UUIDValidator {

    public UUID handleUUID(String stringUuid){
        try{
            UUID uuid = UUID.fromString(stringUuid);
            log.info("Valid UUID: {}", stringUuid);
            return uuid;
        }catch (IllegalArgumentException e){
            log.info("Invalid UUID: {}", stringUuid);
            throw new InvalidUUIDException("Invalid UUID: " + stringUuid, e);
        }
    }

}
