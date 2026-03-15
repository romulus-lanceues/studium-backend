package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionSummary;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {


    /*
        Get session with its user and subject.
        Used by:
        * SessionService -  completeSession
        * SessionService - cancelSession
     */
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject JOIN FETCH s.user WHERE s.id = :sessionId")
    Optional<StudySession> findByIdWithSubjectAndUser(@Param("sessionId") Long sessionId);


    //Consider indexing if the user grows
    @Query("SELECT COUNT (s) FROM StudySession s WHERE s.user.id = :userId")
    Long userSessionsCount(@Param("userId") long userId);

    //Checks if the session exists using the userId and scheduleId BOOLEAN return value
    boolean existsByIdAndUserId(Long sessionId, Long subjectId);

    //Checks if the session exists using the userId and scheduleId OPTIONAL return type
    Optional<StudySession> findByIdAndUserId(Long sessionId, Long userId);


    /*
        Returns the sessions completed since the passed date and time.
     */
    @Query("SELECT s FROM StudySession s WHERE s.endTime >= :cutoff AND s.sessionStatus = :status")
    List<StudySession> findRecentCompletedSessions(@Param("cutoff")LocalDateTime cutoff, @Param("status") SessionStatus status);


    /*
        Retrieves sessions
     */
    @Query("SELECT s FROM StudySession s WHERE s.user.id = :id AND s.sessionStatus = :status")
    List<StudySession> retrieveSessionsWithSpecificStatus( @Param("id") Long userId, @Param("status")SessionStatus status);


    /*
        Used to retrieve session with both COMPLETED and CANCELLED status for a specific time period.
        Used by:
        * DataService - retrieveSessionsForThisWeek
     */
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject WHERE s.endTime >= :startDate "
            + "AND s.endTime <= :endDate AND s.sessionStatus IN :statuses AND s.user.id = :userId")
    List<StudySession> retrieveCompletedAndCancelledSessionsForASpecificTimePeriod( @Param("startDate") LocalDateTime starDate,
                                                                      @Param("endDate") LocalDateTime endDate, @Param("statuses") List<SessionStatus> statuses,
                                                                      @Param("userId") Long userId);

    /*
        Used to retrieve the completed sessions of a specific user for today
     */
    @Query("""
            SELECT COUNT (s) FROM StudySession s WHERE s.user.id = :userId AND s.endTime >= :startOfTheDay
            AND s.endTime < :endOfTheDay AND s.sessionStatus = :status
            """)
    Long countCompletedSessionsToday(@Param("userId") Long userId, @Param("startOfTheDay") LocalDateTime startOfTheDay,
                                                      @Param("endOfTheDay") LocalDateTime endOfTheDay, @Param("status") SessionStatus status);

    Page<StudySession> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT s FROM StudySession s WHERE s.user.id = :userId 
            AND s.startTime >= :startOfTheDay AND s.startTime < :endOfTheDay 
            AND s.sessionStatus IN :statuses
            """)
    Page<StudySession> findSessionsForToday(@Param("userId") Long userId, @Param("startOfTheDay") LocalDateTime startOfTheDay,
                                                @Param("endOfTheDay") LocalDateTime endOfTheDay, @Param("statuses") List<SessionStatus> statuses, Pageable pageable);

    @Query("SELECT s FROM StudySession s WHERE s.subject.id = :subjectId")
    Page<StudySession> findHistoryForSubject(@Param("subjectId") Long subjectId, Pageable pageable);

    @Query("""
            SELECT
            new com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionSummary
            ( CAST( s.endTime AS LocalDate),
            COUNT(s)
            )
            
            FROM StudySession s WHERE s.user.id = :userId
             AND YEAR (s.endTime) = :year
             AND MONTH (s.endTime) = :month
             AND s.sessionStatus = :status
             GROUP BY CAST (s.endTime AS LocalDate)
             ORDER BY CAST (s.endTime AS LocalDate) ASC
            """)
    List<CompletedSessionSummary> getCompletedSessionsForSpecificMonth( @Param("userId") Long userId, @Param("year") Integer year,
                                                                       @Param("month") Integer month, @Param("status") SessionStatus status
                                                                       );

}
