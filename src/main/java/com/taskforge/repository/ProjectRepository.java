package com.taskforge.repository;

import com.taskforge.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // C4: JPQL DISTINCT with path navigation through the task side
    @Query("SELECT DISTINCT t.project FROM Task t WHERE t.teamMember.id = :teamMemberId")
    List<Project> findProjectsOfTeamMember(@Param("teamMemberId") UUID teamMemberId);

    // B1: DERIVED query - the IgnoreCase keyword keeps it case-insensitive,
    // and OrderBy...Asc sorts by projectName from A to Z.
    List<Project> findByClientNameIgnoreCaseOrderByProjectNameAsc(String clientName);
}
