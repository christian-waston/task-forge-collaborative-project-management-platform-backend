package com.taskforge.repository;

import com.taskforge.entity.Task;
import com.taskforge.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // B2: DERIVED query - the enum parameter and OrderBy...Asc bring the tasks
    // of that status back with the earliest dueDate first.
    List<Task> findByStatusOrderByDueDateAsc(TaskStatus status);

    // B3: DERIVED query - the Between keyword matches both dates inclusively.
    List<Task> findByDueDateBetweenOrderByDueDateAsc(LocalDate start, LocalDate end);

    // D1: DERIVED query - used by the safe save to detect a task of the same
    // team member on the same date whose status is not DROPPED.
    boolean existsByTeamMemberIdAndDueDateAndStatusNot(UUID teamMemberId, LocalDate dueDate, TaskStatus status);
}
