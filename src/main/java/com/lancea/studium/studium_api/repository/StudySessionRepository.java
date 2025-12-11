package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    //Get session with its subject
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject " +
            "WHERE s.id = :sessionId")
    Optional<StudySession> getSessionWithSubject (@Param("sessionId") Long subjectId);

    //Get session with its user and subject
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject JOIN FETCH s.user WHERE s.id = :sessionId")
    Optional<StudySession> findByIdWithSubjectAndUser(@Param("sessionId") Long sessionId);

    boolean existsByIdAndUserId(Long sessionId, Long subjectId);

    Optional<StudySession> findByIdAndUserId(Long sessionId, Long userId);
}
