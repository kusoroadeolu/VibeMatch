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
            dtos.add(new GenreDto(tasteWrapper.getName(), mathUtils.round(tasteWrapper.getPercentage()), tasteWrapper.getCount()));
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

    public String discoveryRole(double discoveryPattern){
        String role = "";

        if(discoveryPattern >= 0 && discoveryPattern < 0.45){
            return "Loyalist";
        }else if(discoveryPattern >= 0.45 && discoveryPattern < 0.55){
            return "Balanced";
        }else if(discoveryPattern >= 0.55 && discoveryPattern <= 1){
            return "Explorer";
        }else{
            return "Weird";
        }
    }
}
