package com.victor.VibeMatch.compatibility.embeddables;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CompatibilityWrapper implements Serializable {
    private String name;
    private double your;
    private double their;
}
