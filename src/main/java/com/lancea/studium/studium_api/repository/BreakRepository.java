package com.lancea.studium.studium_api.repository;

import com.lancea.studium.studium_api.entity.Break;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreakRepository extends JpaRepository<Break, Long> {
    // Basic CRUD operations are automatically provided by JpaRepository

}