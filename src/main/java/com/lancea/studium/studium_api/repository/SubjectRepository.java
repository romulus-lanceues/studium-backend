package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.dto.projection.SubjectGoalProjection;
import com.lancea.studium.studium_api.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    //Finds all the subjects owned by a user using the user's id
    Page<Subject> findByUserId(Long userId, Pageable pageable);

    //Checks if a subject with the given ID exists AND belongs to the specified user.
    boolean existsByIdAndUserId(Long subjectId, Long userId);

    //Checks if the subject exists, belong to a user and returns the subject and user using join fetch
    @Query("SELECT s FROM Subject s JOIN FETCH s.user " +
            "WHERE s.id = :subjectId AND s.user.id = :userId")
    Optional<Subject> findByIdAndUserId(@Param("subjectId") Long subjectId,
                                        @Param("userId") Long userId);


    @Query(" SELECT COUNT (s) FROM Subject s WHERE s.user.id = :userId")
    Long subjectCount(@Param("userId") Long userId);

    //Query for all the total study time of the user for each of their subject sum them up and return the result
    @Query("""
            SELECT
                COALESCE(SUM(s.totalStudyTime), 0) 
            FROM Subject s 
                WHERE s.user.id = :userId
            """  )
    Long getUserTotalStudyTime(@Param("userId") Long userId);

    /**
     * Sums the weekly target session per subject of the user
     * @param userId
     * @return the sum of all the weekly target session
     */

    @Query("""
            SELECT
                COALESCE( SUM(s.weeklyGoalSessions), 0)
            FROM Subject s
                WHERE s.user.id = :userId
            """)
    Long userTargetSessionPerWeek(@Param("userId") Long userId);

    /**
     * Finds each subject the user owns, its target session per week,
     * and how many sessions the user has completed so far
     * @param userId
     * @param weekStart
     * @return a list of subject goal projection
     */

    @Query(value = """
            SELECT
                sub.id      AS subjectId,
                sub.name    AS subjectName,
                sub.weekly_goal_sessions    AS weeklyGoal,
                COUNT(ss.id) FILTER (WHERE ss.session_status = 'COMPLETED'
                AND ss.start_time >= :weekStart)    AS completedThisWeek
            FROM subjects sub
            LEFT JOIN study_sessions ss
                ON ss.subject_id = sub.id
                AND ss.session_type = 'WORK'
            WHERE sub.user_id = :userId
                AND sub.weekly_goal_sessions IS NOT NULL
            GROUP BY sub.id, sub.name, sub.weekly_goal_sessions
            ORDER BY sub.name ASC
            """, nativeQuery = true)
    List<SubjectGoalProjection> findSubjectGoalProgress(@Param("userId") Long userId, @Param("weekStart") LocalDateTime weekStart);


}
