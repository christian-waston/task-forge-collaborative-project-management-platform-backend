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

    List<Task> findByStatusOrderByDueDateAsc(TaskStatus status);

    List<Task> findByDueDateBetweenOrderByDueDateAsc(LocalDate start, LocalDate end);

    boolean existsByTeamMemberIdAndDueDateAndStatusNot(UUID teamMemberId, LocalDate dueDate, TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksPerStatus();

    @Modifying
    @Query("UPDATE Task t SET t.status = com.taskforge.entity.TaskStatus.DROPPED " +
           "WHERE t.teamMember.id = :teamMemberId AND t.dueDate = :date " +
           "AND t.status <> com.taskforge.entity.TaskStatus.DONE")
    int cancelTasksOfTeamMemberOnDate(@Param("teamMemberId") UUID teamMemberId,
                                      @Param("date") LocalDate date);

    long countByStatus(TaskStatus status);

    long countByDueDate(LocalDate dueDate);
}
