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

    public String save(Task task) {
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            return "Task date cannot be in the past";
        }
        if (task.getTeamMember() != null && task.getDueDate() != null
                && taskRepository.existsByTeamMemberIdAndDueDateAndStatusNot(
                        task.getTeamMember().getId(), task.getDueDate(), TaskStatus.DROPPED)) {
            return "Team member already has a task due on that date";
        }
        if (task.getProject() != null && task.getProject().getId() != null) {
            Optional<Project> project = projectRepository.findById(task.getProject().getId());
            project.ifPresent(task::setProject);
        }
        if (task.getTeamMember() != null && task.getTeamMember().getId() != null) {
            Optional<TeamMember> teamMember = teamMemberRepository.findById(task.getTeamMember().getId());
            teamMember.ifPresent(task::setTeamMember);
        }
        task.setStatus(TaskStatus.OPEN);
        taskRepository.save(task);
        return "Task saved successfully";
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public List<Task> getByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByDueDateAsc(status);
    }

    public List<Task> getByDueDateBetween(LocalDate start, LocalDate end) {
        return taskRepository.findByDueDateBetweenOrderByDueDateAsc(start, end);
    }

    public Page<Task> getPage(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public List<Object[]> getStatsByStatus() {
        return taskRepository.countTasksPerStatus();
    }

    @Transactional
    public String cancelDay(UUID teamMemberId, LocalDate date) {
        int count = taskRepository.cancelTasksOfTeamMemberOnDate(teamMemberId, date);
        return count + " tasks cancelled";
    }

    public Optional<Task> getById(UUID id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> update(UUID id, Task task) {
        Optional<Task> existing = taskRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        task.setId(id);
        task.setStatus(existing.get().getStatus());
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
