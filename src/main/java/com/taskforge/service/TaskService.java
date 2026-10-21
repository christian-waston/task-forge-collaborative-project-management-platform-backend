package com.taskforge.service;

import com.taskforge.entity.Project;
import com.taskforge.entity.Task;
import com.taskforge.entity.TeamMember;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       TeamMemberRepository teamMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public Task save(Task task) {
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
        return taskRepository.save(task);
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> getById(UUID id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> update(UUID id, Task task) {
        if (!taskRepository.existsById(id)) {
            return Optional.empty();
        }
        task.setId(id);
        return Optional.of(save(task));
    }

    public boolean deleteById(UUID id) {
        if (!taskRepository.existsById(id)) {
            return false;
        }
        taskRepository.deleteById(id);
        return true;
    }
}
