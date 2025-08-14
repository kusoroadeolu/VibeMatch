package com.victor.VibeMatch.spotify.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtist {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private int popularity;

    @JsonProperty
    private Set<String> genres;

    private int rank;

}
