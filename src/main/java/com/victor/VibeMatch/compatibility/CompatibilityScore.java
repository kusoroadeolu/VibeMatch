package com.victor.VibeMatch.compatibility;

import com.victor.VibeMatch.compatibility.embeddables.CompatibilityKey;
import com.victor.VibeMatch.compatibility.embeddables.CompatibilityWrapper;
import com.victor.VibeMatch.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        indexes = {
                @Index(name = "u_idx", columnList = "user_id"),
                @Index(name = "tu_idx", columnList = "target_user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CompatibilityScore {

    @EmbeddedId
    private CompatibilityKey key;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("targetUserId")
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column
    private double discoveryCompatibility;

    @Column
    private double tasteCompatibility;

    @ElementCollection(fetch = FetchType.EAGER)
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "artist_name", nullable = false)),
            @AttributeOverride(name = "your", column = @Column(name = "your_rank")),
            @AttributeOverride(name = "their", column = @Column(name = "their_rank"))
    })
    private List<CompatibilityWrapper> sharedArtists;

    @ElementCollection(fetch = FetchType.EAGER)
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "genre_name", nullable = false)),
            @AttributeOverride(name = "your", column = @Column(name = "your_percentage")),
            @AttributeOverride(name = "their", column = @Column(name = "their_percentage"))
    })
    private List<CompatibilityWrapper> sharedGenres;

    @Column(nullable = false)
    private List<String> compatibilityReasons;

    @Column(nullable = false)
    private LocalDateTime lastCalculated;

    @PrePersist
    public void onCreate(){
        this.lastCalculated = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.lastCalculated = LocalDateTime.now();
    }
}
