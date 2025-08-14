package com.victor.VibeMatch.usertrack.recent;

import com.victor.VibeMatch.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "user_id", "track_spotify_id"
                })
        }
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRecentTrack {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Set<String> artistNames;

    @Column(nullable = false, updatable = false)
    private Set<String> artistIds;

    @Column
    private String trackSpotifyId;

    @Column(nullable = false)
    private int ranking;

    @Column(nullable = false)
    private int popularity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
    }


}
