package com.victor.VibeMatch.exceptionhandler;

import com.victor.VibeMatch.exceptions.*;
import com.victor.VibeMatch.userartist.UserArtist;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AuthorizationException.class, JwtException.class})
    public ResponseEntity<ApiError> handleAuthenticationException(RuntimeException e){
        ApiError apiError = new ApiError(e.getMessage(), 401, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoSuchUserException.class)
    public ResponseEntity<ApiError> handleNoSuchUserException(NoSuchUserException e){
        ApiError apiError = new ApiError(e.getMessage(), 404, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserSaveException.class)
    public ResponseEntity<ApiError> handleUserSaveException(UserSaveException e){
        ApiError apiError = new ApiError(e.getMessage(), 500, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // User-Artist related exceptions
    @ExceptionHandler({UserArtistSaveException.class, UserArtistDeletionException.class})
    public ResponseEntity<ApiError> handleUserArtistException(RuntimeException e){
        ApiError apiError = new ApiError(e.getMessage(), 500, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // User-Track related exceptions
    @ExceptionHandler({UserTrackSaveException.class, UserTrackDeletionException.class})
    public ResponseEntity<ApiError> handleUserTrackException(RuntimeException e){
        ApiError apiError = new ApiError(e.getMessage(), 500, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // General/External API exceptions
    @ExceptionHandler({DataFetchException.class, UserSyncException.class})
    public ResponseEntity<ApiError> handleDataAndSyncException(RuntimeException e) {
        ApiError apiError = new ApiError(e.getMessage(), 500, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SpotifyRateLimitException.class)
    public ResponseEntity<ApiError> handleSpotifyRateLimitException(SpotifyRateLimitException e) {
        ApiError apiError = new ApiError(e.getMessage(), 429, LocalDateTime.now());
        return new ResponseEntity<>(apiError, HttpStatus.TOO_MANY_REQUESTS);
    }
}