package com.victor.VibeMatch.tasteprofile.embeddables;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TasteWrapper{
    private String name;

    private double percentage;

    private int count;

    private int ranking;
}
