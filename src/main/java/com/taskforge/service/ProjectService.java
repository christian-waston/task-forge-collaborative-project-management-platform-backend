package com.taskforge.service;

import com.taskforge.entity.Project;
import com.taskforge.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    // B1
    public List<Project> getByClientName(String clientName) {
        return projectRepository.findByClientNameIgnoreCaseOrderByProjectNameAsc(clientName);
    }

    public Optional<Project> getById(UUID id) {
        return projectRepository.findById(id);
    }

    public Optional<Project> update(UUID id, Project project) {
        if (!projectRepository.existsById(id)) {
            return Optional.empty();
        }
        project.setId(id);
        return Optional.of(projectRepository.save(project));
    }

    public boolean deleteById(UUID id) {
        if (!projectRepository.existsById(id)) {
            return false;
        }
        projectRepository.deleteById(id);
        return true;
    }
}
