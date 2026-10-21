package com.taskforge.repository;

import com.taskforge.entity.Task;
import com.taskforge.entity.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, UUID> {

    @Query("SELECT w FROM WorkLog w WHERE w.task.project.projectCode = :projectCode ORDER BY w.loggedDate DESC")
    List<WorkLog> findByProjectCode(@Param("projectCode") String projectCode);

    boolean existsByTask(Task task);
}
