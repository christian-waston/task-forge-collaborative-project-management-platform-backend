package com.taskforge.repository;

import com.taskforge.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // B1: DERIVED query - the IgnoreCase keyword keeps it case-insensitive,
    // and OrderBy...Asc sorts by projectName from A to Z.
    List<Project> findByClientNameIgnoreCaseOrderByProjectNameAsc(String clientName);
}
