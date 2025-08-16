package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.dto.ArtistDto;
import com.victor.VibeMatch.tasteprofile.dto.GenreDto;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TasteProfileMapper {
    public TasteProfileResponseDto responseDto(TasteProfile tasteProfile){
        return new TasteProfileResponseDto(
                tasteProfile.getUser().getId().toString(),
                tasteProfile.getUser().getUsername(),
                tasteProfile.getUser().isPublic(),
                mapTasteWrappersToGenreDtos(tasteProfile.getTopGenres()),
                mapTasteWrappersToArtistDtos(tasteProfile.getTopArtists()),
                tasteProfile.getMainstreamScore(),
                tasteProfile.getDiscoveryPattern(),
                tasteProfile.getLastUpdated()
        );
    }

    public List<GenreDto> mapTasteWrappersToGenreDtos(List<TasteWrapper> tasteWrappers){
        List<GenreDto> dtos = new ArrayList<>();

        for(TasteWrapper tasteWrapper: tasteWrappers){
            dtos.add(new GenreDto(tasteWrapper.getName(), tasteWrapper.getPercentage(), tasteWrapper.getCount()));
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
}
