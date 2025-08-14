package com.victor.VibeMatch.userartist;

import com.victor.VibeMatch.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_artist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "user_id", "artist_spotify_id"
        })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserArtist {    @Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private String artistSpotifyId;

    @Column(nullable = false)
    private int ranking;

    @Column(nullable = false, updatable = false)
    private int popularity;

    @Column(nullable = false, updatable = false)
    private Set<String> genres;

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
