package com.victor.VibeMatch.usertrack;

import com.victor.VibeMatch.usertrack.dtos.UserTrackResponseDto;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import org.springframework.stereotype.Service;

@Service
public class UserTrackMapper {
    //Maps a user track to its dto
    public UserTrackResponseDto responseDto(Object track){
        if(track instanceof UserRecentTrack recent){
            return new UserTrackResponseDto(
                    recent.getTrackSpotifyId(),
                    recent.getName(),
                    recent.getArtistNames(),
                    recent.getUser().getUsername()
            );
        }else if(track instanceof UserTopTrack top){
            return new UserTrackResponseDto(
                    top.getTrackSpotifyId(),
                    top.getName(),
                    top.getArtistNames(),
                    top.getUser().getUsername()
            );
        }
        throw new IllegalArgumentException("Passed object is not a type of User Recent Track or User Top Track");
    }
}
