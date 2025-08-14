package com.victor.VibeMatch.usertrack;

import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.spotify.dto.SpotifyTrack;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserTrackUtils {

    public UserTopTrack buildUserTopTrack(SpotifyTrack spotifyTrack, User user){
        Set<String> artistIds = spotifyTrack.getSpotifyArtists().stream().map(SpotifyArtist::getId).collect(Collectors.toSet());
        Set<String> artistNames = spotifyTrack.getSpotifyArtists().stream().map(SpotifyArtist::getName).collect(Collectors.toSet());
        log.info("Track ID: {}, Name: {}, Popularity: {}", spotifyTrack.getId(), spotifyTrack.getName(), spotifyTrack.getPopularity());
        return UserTopTrack
                .builder()
                .name(spotifyTrack.getName())
                .user(user)
                .trackSpotifyId(spotifyTrack.getId())
                .artistIds(artistIds)
                .artistNames(artistNames)
                .ranking(spotifyTrack.getRank())
                .popularity(spotifyTrack.getPopularity())
                .build();
    }

    public UserRecentTrack buildUserRecentTrack(SpotifyTrack spotifyTrack, User user){
        Set<String> artistIds = spotifyTrack.getSpotifyArtists().stream().map(SpotifyArtist::getId).collect(Collectors.toSet());
        Set<String> artistNames = spotifyTrack.getSpotifyArtists().stream().map(SpotifyArtist::getName).collect(Collectors.toSet());
        log.info("Track ID: {}, Name: {}, Popularity: {}", spotifyTrack.getId(), spotifyTrack.getName(), spotifyTrack.getPopularity());
        return UserRecentTrack
                .builder()
                .name(spotifyTrack.getName())
                .user(user)
                .trackSpotifyId(spotifyTrack.getId())
                .artistIds(artistIds)
                .artistNames(artistNames)
                .ranking(spotifyTrack.getRank())
                .popularity(spotifyTrack.getPopularity())
                .build();
    }

}
