package com.victor.VibeMatch.connections;

import com.victor.VibeMatch.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "user_a_id","user_b_id"
}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_a_id", nullable = false, updatable = false)
    private User userA;

    @ManyToOne
    @JoinColumn(name = "user_b_id", nullable = false, updatable = false)
    private User userB;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false, updatable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false, updatable = false)
    private User receiver;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime connectedSince;

    private boolean isConnected = false;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
    }

    public void acceptConnection(){
        this.connectedSince = LocalDateTime.now();
        this.isConnected = true;
    }

}
