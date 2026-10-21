package com.taskforge.service;

import com.taskforge.entity.Project;
import com.taskforge.entity.Task;
import com.taskforge.entity.TaskStatus;
import com.taskforge.entity.TeamMember;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.TeamMemberRepository;
import com.taskforge.repository.WorkLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkLogRepository workLogRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       TeamMemberRepository teamMemberRepository,
                       WorkLogRepository workLogRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.workLogRepository = workLogRepository;
    }

    // D1: safe saving. The controller maps the returned message to its HTTP status.
    public String save(Task task) {
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            return "Task date cannot be in the past";
        }
        if (task.getTeamMember() != null && task.getDueDate() != null
                && taskRepository.existsByTeamMemberIdAndDueDateAndStatusNot(
                        task.getTeamMember().getId(), task.getDueDate(), TaskStatus.DROPPED)) {
            return "Team member already has a task due on that date";
        }
        // The project and the team member arrive as detached references with only their id,
        // so load the managed versions before saving the task.
        if (task.getProject() != null && task.getProject().getId() != null) {
            Optional<Project> project = projectRepository.findById(task.getProject().getId());
            project.ifPresent(task::setProject);
        }
        if (task.getTeamMember() != null && task.getTeamMember().getId() != null) {
            Optional<TeamMember> teamMember = teamMemberRepository.findById(task.getTeamMember().getId());
            teamMember.ifPresent(task::setTeamMember);
        }
        // A new task always starts as OPEN, whatever the JSON body contains.
        task.setStatus(TaskStatus.OPEN);
        taskRepository.save(task);
        return "Task saved successfully";
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    // B2
    public List<Task> getByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByDueDateAsc(status);
    }

    // B3
    public List<Task> getByDueDateBetween(LocalDate start, LocalDate end) {
        return taskRepository.findByDueDateBetweenOrderByDueDateAsc(start, end);
    }

    // Bonus: tasks page by page, the Pageable is built in the controller
    public Page<Task> getPage(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    // F1: tasks per status, one row per status with its count
    public List<Object[]> getStatsByStatus() {
        return taskRepository.countTasksPerStatus();
    }

    // F2: cancel every task of that team member on that date, except DONE ones
    @Transactional
    public String cancelDay(UUID teamMemberId, LocalDate date) {
        int count = taskRepository.cancelTasksOfTeamMemberOnDate(teamMemberId, date);
        return count + " tasks cancelled";
    }

    public Optional<Task> getById(UUID id) {
        return taskRepository.findById(id);
    }

    // D2: the general PUT update no longer changes the status, it is kept as it is.
    public Optional<Task> update(UUID id, Task task) {
        Optional<Task> existing = taskRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        task.setId(id);
        // keep the current status: only PATCH /status can change it
        task.setStatus(existing.get().getStatus());
        // The project and the team member arrive as detached references with only their id,
        // so load the managed versions before saving the task.
        if (task.getProject() != null && task.getProject().getId() != null) {
            Optional<Project> project = projectRepository.findById(task.getProject().getId());
            project.ifPresent(task::setProject);
        }
        if (task.getTeamMember() != null && task.getTeamMember().getId() != null) {
            Optional<TeamMember> teamMember = teamMemberRepository.findById(task.getTeamMember().getId());
            teamMember.ifPresent(task::setTeamMember);
        }
        return Optional.of(taskRepository.save(task));
    }

    // D2: status flow. DONE and DROPPED are final, only the allowed moves pass.
    public String updateStatus(UUID id, TaskStatus newStatus) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return "The task with that id does not exist";
        }
        TaskStatus current = task.get().getStatus();
        boolean allowed =
                (current == TaskStatus.OPEN && (newStatus == TaskStatus.IN_PROGRESS || newStatus == TaskStatus.DROPPED))
                        || (current == TaskStatus.IN_PROGRESS && (newStatus == TaskStatus.DONE || newStatus == TaskStatus.DROPPED));
        if (!allowed) {
            return "Cannot change status from " + current + " to " + newStatus;
        }
        task.get().setStatus(newStatus);
        taskRepository.save(task.get());
        return "Task status updated successfully";
    }

    public boolean deleteById(UUID id) {
        if (!taskRepository.existsById(id)) {
            return false;
        }
        taskRepository.deleteById(id);
        return true;
    }

    // E3: a task that has work logs can no longer be deleted
    public String deleteTask(UUID id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return "The task with that id does not exist";
        }
        if (workLogRepository.existsByTask(task.get())) {
            return "That task has work logs, delete them first";
        }
        taskRepository.delete(task.get());
        return "Task deleted successfully";
    }
}
