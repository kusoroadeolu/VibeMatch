package com.victor.VibeMatch.userartist.mapper;

import com.victor.VibeMatch.spotify.dto.SpotifyArtist;
import com.victor.VibeMatch.user.User;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.userartist.UserArtistResponseDto;
import org.springframework.stereotype.Service;

@Service
public class UserArtistMapper {
    public UserArtistResponseDto responseDto(UserArtist userArtist){
        return new UserArtistResponseDto(
                userArtist.getArtistSpotifyId(),
                userArtist.getName(),
                userArtist.getPopularity(),
                userArtist.getGenres(),
                userArtist.getRanking(),
                userArtist.getUser().getUsername()
        );
    }

    public UserArtist buildUserArtist(User user, SpotifyArtist artist){
        return UserArtist
                .builder()
                .user(user)
                .artistSpotifyId(artist.getId())
                .name(artist.getName())
                .ranking(artist.getRank())
                .genres(artist.getGenres())
                .popularity(artist.getPopularity())
                .build();
    }
}
