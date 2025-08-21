package com.victor.VibeMatch.user;

import com.victor.VibeMatch.compatibility.CompatibilityScore;
import com.victor.VibeMatch.connections.Connection;
import com.victor.VibeMatch.recommendations.Recommendation;
import com.victor.VibeMatch.security.Role;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.userartist.UserArtist;
import com.victor.VibeMatch.usertrack.recent.UserRecentTrack;
import com.victor.VibeMatch.usertrack.top.UserTopTrack;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"spotify_id", "username"})
}, indexes = @Index(name = "si_index", columnList = "spotifyId"))
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, updatable = false, nullable = false)
    private String spotifyId;

    @Column(nullable = false, length = 32)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastSyncedAt;

    //TODO Encrypt this
    @Column(nullable = false)
    private String refreshToken;

    private Role role;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean isPublic = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserArtist> userArtists;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRecentTrack> userRecentTracks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTopTrack> userTopTracks;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private TasteProfile tasteProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompatibilityScore> compatibilityScore;


    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }


}
