package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.dto.projection.*;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionSummary;
import com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionsPerSubject;
import com.lancea.studium.studium_api.dto.response.single_response.DurationBucketDTO;
import com.lancea.studium.studium_api.shared.enums.BreakDownPeriod;
import com.lancea.studium.studium_api.shared.enums.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;
import com.lancea.studium.studium_api.shared.enums.SessionType;
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
        * SessionService - completeSession
        * SessionService - cancelSession
     */
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject JOIN FETCH s.user WHERE s.id = :sessionId")
    Optional<StudySession> findByIdWithSubjectAndUser(@Param("sessionId") Long sessionId);


    //Consider indexing if the user grows
    @Query("SELECT COUNT (s) FROM StudySession s WHERE s.user.id = :userId")
    Long userSessionsCount(@Param("userId") long userId);

    //Checks if the session exists using the userId and scheduleId OPTIONAL return type
    Optional<StudySession> findByIdAndUserId(Long sessionId, Long userId);



    // Returns the sessions completed since the passed date and time.

    @Query("SELECT s FROM StudySession s WHERE s.endTime >= :cutoff AND s.sessionStatus = :status")
    List<StudySession> findRecentCompletedSessions(@Param("cutoff")LocalDateTime cutoff, @Param("status") SessionStatus status);


    // Retrieves specific sessions according to their status (Paging must be implemented)
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

    //*****
    @Query("SELECT s FROM StudySession s JOIN FETCH s.subject WHERE s.endTime >= :startDate "
            + "AND s.endTime <= :endDate AND s.sessionStatus = :status AND s.user.id = :userId")
    List<StudySession> retrieveSessionsForASpecificTimePeriod (@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate,
                                                                        @Param("endDate") LocalDateTime endDate, @Param("status") SessionStatus status);

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
    Page<StudySession> findSessionsForToday(@Param("userId") Long userId,
                                            @Param("startOfTheDay") LocalDateTime startOfTheDay,
                                            @Param("endOfTheDay") LocalDateTime endOfTheDay,
                                            @Param("statuses") List<SessionStatus> statuses, Pageable pageable);

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
    List<CompletedSessionSummary> getCompletedSessionsForSpecificMonth( @Param("userId") Long userId,
                                                                        @Param("year") Integer year,
                                                                       @Param("month") Integer month,
                                                                        @Param("status") SessionStatus status
                                                                       );



    @Query("""
            SELECT 
            new com.lancea.studium.studium_api.dto.response.single_response.CompletedSessionsPerSubject
            ( s.subject.name, COUNT(s), s.subject.color )
            
            FROM StudySession s WHERE s.user.id = :userId
            AND YEAR (s.endTime) = :year
            AND MONTH (s.endTime) =:month
            AND s.sessionStatus = :sessionStatus
            GROUP BY (s.subject.name, s.subject.color)
            ORDER BY (s.subject.name) ASC
            """)

    public List<CompletedSessionsPerSubject> getCompletedSessionsByTimePeriod(@Param("userId") Long userId,
                                                                              @Param("year") Integer year,
                                                                              @Param("month") Integer month,
                                                                              @Param("sessionStatus") SessionStatus sessionStatus);

    @Query("""
            SELECT COUNT(s) FROM StudySession s
            WHERE s.user.id = :userId
            AND s.sessionStatus = :sessionStatus
            AND s.endTime >= :windowStart
            """)
      long countSpecificSessionWithinSpecificTimePeriod(@Param("userId") Long userId,
                                                        @Param("sessionStatus") SessionStatus sessionStatus,
                                                        @Param("windowStart")LocalDateTime windowStart);


    // FOCUS RECOMMENDATION QUERIES

    //Sort every session based on their planned duration status
    @Query("""
            SELECT new com.lancea.studium.studium_api.dto.response.single_response.DurationBucketDTO(
            s.plannedDurationMinutes,
            COUNT(s),
            SUM(CASE WHEN s.sessionStatus = :completed THEN 1 ELSE 0 END),
            CAST(SUM(CASE WHEN s.sessionStatus = :completed THEN 1 ELSE 0 END) AS double) / COUNT(s),
            AVG(s.interruptionsCount)
            )
            
            FROM StudySession s
            WHERE s.user.id = :userId
            AND s.sessionType = :workType
            AND s.sessionStatus IN (:completed, :cancelled)
            AND s.startTime >= :since
            GROUP BY s.plannedDurationMinutes
            ORDER BY s.plannedDurationMinutes ASC
            """)
    List<DurationBucketDTO> findDurationBucketsByUserId(@Param("userId") Long userId,
                                                        @Param("workType")SessionType workType,
                                                        @Param("completed") SessionStatus completed,
                                                        @Param("cancelled") SessionStatus cancelled,
                                                        @Param("since") LocalDateTime since);


    /**
     * Retrieves every time period within a specific time frame when the user completed a session
     * Due to the complex nature of a projection was used instead of the traditional DTO
     * @param userId
     * @param since
     * @return List of PeakHourProjection
     */
    @Query(value = """
            SELECT
                EXTRACT(HOUR FROM start_time)::INTEGER     AS  hour,
                COUNT(*)    AS  sessions,
                ROUND(  COUNT(*) FILTER (WHERE session_status = 'COMPLETED')::DECIMAL
                / NULLIF(COUNT(*), 0), 2)       AS  completionRate
            FROM study_sessions
            WHERE user_id = :userId
                AND session_type = 'WORK'
                AND start_time >= :since
            GROUP BY EXTRACT(HOUR FROM start_time)
        ORDER BY sessions DESC
        """, nativeQuery = true)
    List<PeakHourProjection> findPeakHoursByUser(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );


    @Query("""
            SELECT COUNT(s)
            FROM StudySession s
            WHERE s.user.id = :userId
            AND s.sessionType = :workType
            AND s.sessionStatus IN (:completed, :cancelled)
            AND s.startTime >= :since
            """)
    Long countEligibleSessionByUserId(@Param("userId") Long userId,
                                      @Param("workType") SessionType workType,
                                      @Param("completed") SessionStatus completed,
                                      @Param("cancelled") SessionStatus cancelled,
                                      @Param("since") LocalDateTime since);
    //ANALYTICS QUERIES

    /**
     * Due to the complex nature of the query, this QUERY uses PostgresSQL-specific syntax that JPQL can't support
     * Aggregates important data of the user such as totalSessions, completedSessions, completionRate, and totalFocusMinutes
     * @param userId
     * @return SummaryStatsProjection that contains user summary data
     */

    @Query(value = """
            SELECT
                COUNT(*)    AS totalSessions,
                COUNT(*) FILTER (WHERE session_status = 'COMPLETED')    AS completedSessions,
                ROUND (COUNT(*) FILTER (WHERE session_status = 'COMPLETED'):: DECIMAL
                / NULLIF(COUNT(*), 0), 2 )  AS completionRate,
                COALESCE( SUM(actual_duration_minutes)
                    FILTER (WHERE session_status = 'COMPLETED'), 0)     AS totalFocusMinutes,
                COALESCE( AVG(interruptions_count)
                    FILTER(WHERE session_status = 'COMPLETED'), 0)      AS averageInterruptions
            FROM study_sessions
            WHERE user_id = :userId
            """, nativeQuery = true)
    SummaryStatsProjection fetchSummaryStats(@Param("userId") Long userId);

    /**
     *Retrieves raw productivity data of the user, includes:
     * - completionRate
     * - consistencyRate
     * - totalSessions
     * @param userId
     * @param since
     * @param until
     * @return ProductivityRawProjection that aggregates all of this data together.
     */

    @Query(value = """
            SELECT
                ROUND( COUNT(*) FILTER (WHERE session_status = 'COMPLETED')::DECIMAL
                / NULLIF(COUNT(*), 0) , 4)  AS completionRate,
                ROUND(
                 AVG (
                    LEAST(actual_duration_minutes::DECIMAL / NULLIF(planned_duration_minutes, 0), 1.0)
                    )
                 FILTER(WHERE session_status = 'COMPLETED'), 4  )   AS  consistencyRate,
                 COUNT(*)   AS  totalSessions,
                 COALESCE( AVG(interruptions_count)
                    FILTER (WHERE session_status = 'COMPLETED'), 0) AS averageInterruptions
            FROM study_sessions
            WHERE user_id = :userId
                AND session_type = 'WORK'
                AND start_time >= :since
                AND start_time < :until
            """, nativeQuery = true)
    ProductivityRawProjection fetchRawUserProductivityData(@Param("userId")Long userId,
                                                           @Param("since") LocalDateTime since,
                                                           @Param("until") LocalDateTime until);


    /**
     * These queries are pretty similar but has distinct DATE_TRUNC function arguments.
     * The reason for this is that Postgres needs its function arguments defined at compile time to know what action it must perform.
     * @param userId
     * @param since
     * @return
     */

    @Query(value = """
            SELECT
                DATE_TRUNC('day', start_time)   AS periodStart,
                COUNT(*)    AS sessions,
                COALESCE( SUM(actual_duration_minutes) 
                    FILTER (WHERE session_status = 'COMPLETED'), 0) AS focusMinutes,
                ROUND( COUNT(*) FILTER (WHERE session_status = 'COMPLETED')::DECIMAL
                    / NULLIF(COUNT(*), 0), 2)   AS completionRate
            FROM study_sessions
            WHERE user_id = :userId
                AND session_type = 'WORK'
                AND start_time >= :since
            GROUP BY DATE_TRUNC('day', start_time)
            ORDER BY DATE_TRUNC('day', start_time) ASC
            """, nativeQuery = true)
    List<BreakDownProjection> findDailyBreakDown(@Param("userId") Long userId,
                                                        @Param("since") LocalDateTime since);


    @Query(value = """
            SELECT
                DATE_TRUNC('week', start_time)   AS periodStart,
                COUNT(*)    AS sessions,
                COALESCE( SUM(actual_duration_minutes) 
                    FILTER (WHERE session_status = 'COMPLETED'), 0) AS focusMinutes,
                ROUND( COUNT(*) FILTER (WHERE session_status = 'COMPLETED')::DECIMAL
                    / NULLIF(COUNT(*), 0), 2)   AS completionRate
            FROM study_sessions
            WHERE user_id = :userId
                AND session_type = 'WORK'
                AND start_time >= :since
            GROUP BY DATE_TRUNC('week', start_time)
            ORDER BY DATE_TRUNC('week', start_time) ASC
            """, nativeQuery = true)
    List<BreakDownProjection> findWeeklyBreakdown(@Param("userId") Long userId,
                                                  @Param("since") LocalDateTime since);


    @Query(value = """
            SELECT
                DATE_TRUNC('month', start_time)   AS periodStart,
                COUNT(*)    AS sessions,
                COALESCE( SUM(actual_duration_minutes) 
                    FILTER (WHERE session_status = 'COMPLETED'), 0) AS focusMinutes,
                ROUND( COUNT(*) FILTER (WHERE session_status = 'COMPLETED')::DECIMAL
                    / NULLIF(COUNT(*), 0), 2)   AS completionRate
            FROM study_sessions
            WHERE user_id = :userId
                AND session_type = 'WORK'
                AND start_time >= :since
            GROUP BY DATE_TRUNC('month', start_time)
            ORDER BY DATE_TRUNC('month', start_time) ASC
            """, nativeQuery = true)
    List<BreakDownProjection> findMonthlyBreakDown(@Param("userId") Long userId,
                                                   @Param("since") LocalDateTime since);



}
