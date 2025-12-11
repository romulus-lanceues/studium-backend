package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    //Finds all the subjects owned by a user using the user's id
    List<Subject> findByUserId(Long userId);

    //Checks if a subject with the given ID exists AND belongs to the specified user.
    boolean existsByIdAndUserId(Long subjectId, Long userId);

    //Checks if the subject exists, belong to a user and returns the subject and user using join fetch
    @Query("SELECT s FROM Subject s JOIN FETCH s.user " +
            "WHERE s.id = :subjectId AND s.user.id = :userId")
    Optional<Subject> findByIdAndUserId(@Param("subjectId") Long subjectId,
                                        @Param("userId") Long userId);
}
