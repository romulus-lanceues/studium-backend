package com.lancea.studium.studium_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 500)
    private String description;

    @Column(name = "weekly_goal_sessions")
    private Integer weeklyGoalSessions;

    @Builder.Default
    @Column(name = "total_study_time")
    private Integer totalStudyTime = 0;

    @Builder.Default
    @Column(name = "pomodoros_completed")
    private Integer pomodorosCompleted = 0;

    @Column(name = "last_session")
    private LocalDate lastSession;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //Relationships:

    //Many subjects belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //Many sessions belong to one subject
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudySession> sessions = new ArrayList<>();

    public void increasePomodoroCompleted(){
        this.pomodorosCompleted++;
    }

    public void increaseStudyTime(int latestStudyTime){
        this.totalStudyTime = totalStudyTime + latestStudyTime;
    }
}
