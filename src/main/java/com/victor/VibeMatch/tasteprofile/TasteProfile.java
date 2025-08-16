package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.tasteprofile.embeddables.TasteWrapper;
import com.victor.VibeMatch.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasteProfile {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "taste_profile_artists", joinColumns = @JoinColumn(name = "taste_profile_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "genre_name")),
            @AttributeOverride(name = "percentage", column = @Column(name = "genre_percentage")),
            @AttributeOverride(name = "count", column = @Column(name = "genre_count")),
            @AttributeOverride(name = "ranking", column = @Column(name = "genre_ranking"))
    })
    private List<TasteWrapper> topGenres;

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "taste_profile_genres", joinColumns = @JoinColumn(name = "taste_profile_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "artist_name")),
            @AttributeOverride(name = "percentage", column = @Column(name = "artist_percentage")),
            @AttributeOverride(name = "count", column = @Column(name = "artist_count")),
            @AttributeOverride(name = "ranking", column = @Column(name = "artist_ranking"))
    })
    private List<TasteWrapper> topArtists;

    @Column(nullable = false)
    private double mainstreamScore;

    @Column(nullable = false)
    private double discoveryPattern;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void onCreate(){
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        lastUpdated = LocalDateTime.now();
    }





}
