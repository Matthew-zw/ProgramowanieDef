package com.example.projekt.repository;


import com.example.projekt.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {
    @Query("SELECT p FROM Project p JOIN p.assignedUsers u WHERE u.id = :userId")
    List<Project> findProjectsByAssignedUserId(@Param("userId") Long userId);
}
