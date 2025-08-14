package com.victor.VibeMatch.userartist;

import java.util.Set;

public record UserArtistResponseDto(String id,
                                    String name,
                                    int popularity,
                                    Set<String> genres,
                                    int rank,
                                    String ownedBy) {

}
