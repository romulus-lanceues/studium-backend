package com.lancea.studium.studium_api.entity;


import com.lancea.studium.studium_api.shared.enums.Role;
import com.lancea.studium.studium_api.shared.interfaces.Streakable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
//Lombok Annotations
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Streakable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "streak")
    @Builder.Default
    private Integer streak = 0;

    @Column(name = "last_session")
    @Builder.Default
    //Set the last session to a very long time just to avoid null pointer exception for new users
    private LocalDate lastSession = LocalDate.now().minusYears(20);

    //Highest session count within a day
    @Column(name = "highest_session")
    @Builder.Default
    private Integer highestSession = 0;

    @Column(name = "longest_streak")
    @Builder.Default
    private Integer longestStreak = 0;

    //Relationships:

    //One user has many subjects
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subject> subjects = new ArrayList<>();

    //One user has many sessions
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudySession> sessions = new ArrayList<>();

    public void increaseStreak(){
        streak++;
    }

}
