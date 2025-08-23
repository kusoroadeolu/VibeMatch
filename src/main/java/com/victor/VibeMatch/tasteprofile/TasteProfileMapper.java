package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.math.MathUtils;
import com.victor.VibeMatch.tasteprofile.dto.ArtistDto;
import com.victor.VibeMatch.tasteprofile.dto.GenreDto;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TasteProfileMapper {

    private final MathUtils mathUtils;

    public TasteProfileResponseDto responseDto(TasteProfile tasteProfile){
        return new TasteProfileResponseDto(
                tasteProfile.getUser().getId().toString(),
                tasteProfile.getUser().getUsername(),
                tasteProfile.getUser().isPublic(),
                mapTasteWrappersToGenreDtos(tasteProfile.getTopGenres()),
                mapTasteWrappersToArtistDtos(tasteProfile.getTopArtists()),
                mathUtils.round(tasteProfile.getMainstreamScore()),
                discoveryRole(tasteProfile.getDiscoveryPattern()),
                tasteProfile.getLastUpdated()
        );
    }

    public List<GenreDto> mapTasteWrappersToGenreDtos(List<TasteWrapper> tasteWrappers){
        List<GenreDto> dtos = new ArrayList<>();

        for(TasteWrapper tasteWrapper: tasteWrappers){
            String str = firstCharUpperCase(tasteWrapper.getName());
            dtos.add(new GenreDto(str, mathUtils.round(tasteWrapper.getPercentage()), tasteWrapper.getCount()));
        }
        return dtos;
    }



    public List<ArtistDto> mapTasteWrappersToArtistDtos(List<TasteWrapper> tasteWrappers){
        List<ArtistDto> dtos = new ArrayList<>();

        for(TasteWrapper tasteWrapper: tasteWrappers){
            dtos.add(new ArtistDto(tasteWrapper.getName(), tasteWrapper.getRanking()));
        }
        return dtos;
    }

    private String firstCharUpperCase(String str){
        return str.trim().substring(0, 1).toUpperCase()
                + str.substring(1).toLowerCase();
    }

    private String discoveryRole(double discoveryPattern){
        String role = "";

        if(discoveryPattern >= 0 && discoveryPattern < 0.45){
            return "Your playlist is your comfort zone";
        }else if(discoveryPattern >= 0.45 && discoveryPattern < 0.55){
            return "You mix old favorites with new finds";
        }else if(discoveryPattern >= 0.55 && discoveryPattern <= 1){
            return "You live for that perfect deep cut";
        }else{
            return "Your taste doesn't fit any box";
        }
    }
}
