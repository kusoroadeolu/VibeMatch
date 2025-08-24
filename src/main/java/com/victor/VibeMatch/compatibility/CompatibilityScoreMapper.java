package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.dtos.ArtistCompatibilityDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityScoreResponseDto;
import com.victor.VibeMatch.compatibility.dtos.CompatibilityUserDto;
import com.victor.VibeMatch.compatibility.dtos.GenreCompatibilityDto;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.math.MathUtils;
import com.victor.VibeMatch.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompatibilityScoreMapper {

    private final MathUtils mathUtils;

    public CompatibilityScoreResponseDto responseDto(CompatibilityScore score){
        return new CompatibilityScoreResponseDto(
            userDto(score.getUser()),
            userDto(score.getTargetUser()),
            mathUtils.round(score.getDiscoveryCompatibility()),
            mathUtils.round(score.getTasteCompatibility()),
            artistDtos(score.getSharedArtists()),
            genreDtos(score.getSharedGenres()),
            score.getCompatibilityReasons(),
            score.getLastCalculated()
        );
    }

    public CompatibilityUserDto userDto(User user){
        return new CompatibilityUserDto(
                user.getId().toString(),
                user.getUsername()
        );
    }

    public List<ArtistCompatibilityDto> artistDtos(List<CompatibilityWrapper> artists){
        List<ArtistCompatibilityDto> dtos = new ArrayList<>();
        for(CompatibilityWrapper artist: artists){
            dtos.add(new ArtistCompatibilityDto(artist.getName(), (int)artist.getYour(), (int)artist.getTheir()));
        }
        return dtos;
    }

    public List<GenreCompatibilityDto> genreDtos(List<CompatibilityWrapper> genres){
        List<GenreCompatibilityDto> dtos = new ArrayList<>();
        for(CompatibilityWrapper genre: genres){
            double yourPercentage = genre.getYour();
            double theirPercentage = genre.getTheir();
            dtos.add(new GenreCompatibilityDto(genre.getName(), mathUtils.round(yourPercentage) * 100, mathUtils.round(theirPercentage) * 100));
        }
        return dtos;
    }

}
