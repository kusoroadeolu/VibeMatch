package com.victor.VibeMatch.tasteprofile.embeddables;

import org.springframework.stereotype.Service;

@Service
public class TasteWrapperUtils {
    public TasteWrapper buildTasteWrapper(String name, double percentage, int count){
        return TasteWrapper
                .builder()
                .name(name)
                .percentage(percentage)
                .count(count)
                .build();
    }

    public TasteWrapper buildTasteWrapper(String name, int rank){
        return TasteWrapper
                .builder()
                .name(name)
                .ranking(rank)
                .build();
    }
}
