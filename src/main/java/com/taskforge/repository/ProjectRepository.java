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

    @Query("SELECT DISTINCT t.project FROM Task t WHERE t.teamMember.id = :teamMemberId")
    List<Project> findProjectsOfTeamMember(@Param("teamMemberId") UUID teamMemberId);

    List<Project> findByClientNameIgnoreCaseOrderByProjectNameAsc(String clientName);
}
