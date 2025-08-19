package com.victor.VibeMatch.compatibility.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CompatibilityKey implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

}
