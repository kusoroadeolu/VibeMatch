package com.victor.VibeMatch.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyTopData<T> {
    @JsonProperty
    private ArrayList<T> items;
    @Setter
    @JsonProperty
    private String href;
    @Setter
    @JsonProperty
    private Integer limit;
    @Setter
    @JsonProperty
    private String next;
    @Setter
    @JsonProperty
    private Integer offset;
    @Setter
    @JsonProperty
    private String previous;
    @Setter
    @JsonProperty
    private Integer total;

    @JsonSetter("items")
    public void setItemRanks(ArrayList<T> items){
        this.items = items;
        if(items != null){
            for(int i = 0; i < items.size(); i++){
                T item = items.get(i);

                if(item instanceof SpotifyArtist){
                    ((SpotifyArtist) item).setRank(i + 1);
                }else if(item instanceof SpotifyTrack){
                    ((SpotifyTrack)item).setRank(i + 1);
                }
            }
        }
    }

}
