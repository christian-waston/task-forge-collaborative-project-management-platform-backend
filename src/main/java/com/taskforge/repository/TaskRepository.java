package com.taskforge.repository;

import com.taskforge.entity.Task;
import com.taskforge.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // F1: JPQL GROUP BY + COUNT, one row per status that has tasks, List<Object[]>
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksPerStatus();

    // F2: JPQL UPDATE, one single query with @Modifying, no Java loop
    @Modifying
    @Query("UPDATE Task t SET t.status = com.taskforge.entity.TaskStatus.DROPPED " +
           "WHERE t.teamMember.id = :teamMemberId AND t.dueDate = :date " +
           "AND t.status <> com.taskforge.entity.TaskStatus.DONE")
    int cancelTasksOfTeamMemberOnDate(@Param("teamMemberId") UUID teamMemberId,
                                      @Param("date") LocalDate date);

    // Bonus used later
    long countByStatus(TaskStatus status);

    // Bonus used later
    long countByDueDate(LocalDate dueDate);
}
