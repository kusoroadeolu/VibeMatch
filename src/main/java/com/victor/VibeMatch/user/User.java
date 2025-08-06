package com.victor.VibeMatch.user;

import com.victor.VibeMatch.security.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"spotify_id", "username"})
})
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

    @Column(nullable = false)
    private String refreshToken;

    private Role role;

    @Column(nullable = false)
    private String country;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

}
