package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.entity.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    //Get session with its subject
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject " +
            "WHERE s.id = :sessionId")
    Optional<StudySession> getSessionWithSubject (@Param("sessionId") Long subjectId);

    //Get session with its user and subject
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject JOIN FETCH s.user WHERE s.id = :sessionId")
    Optional<StudySession> findByIdWithSubjectAndUser(@Param("sessionId") Long sessionId);

    //Checks if the session exists using the userId and scheduleId BOOLEAN return value
    boolean existsByIdAndUserId(Long sessionId, Long subjectId);

    //Checks if the session exists using the userId and scheduleId OPTIONAL return type
    Optional<StudySession> findByIdAndUserId(Long sessionId, Long userId);

    //Returns the sessions completed since the passed date and time
    @Query("SELECT s FROM StudySession s WHERE s.endTime >= :cutoff AND s.sessionStatus = :status")
    List<StudySession> findRecentCompletedSessions(@Param("cutoff")LocalDateTime cutoff, @Param("status") SessionStatus status);

    @Query("SELECT s FROM StudySession s WHERE s.user.id = :id AND s.sessionStatus = :status")
    List<StudySession> retrieveSessionsWithSpecificStatus( @Param("id") Long userId, @Param("status")SessionStatus status);

}
