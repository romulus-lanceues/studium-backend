package com.lancea.studium.studium_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(length = 512)
    private String deviceInfo;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    //Automatically generate the createdAt value before adding the entry to DB table
    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }


}
