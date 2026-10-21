package com.taskforge.controller;

import com.taskforge.entity.Project;
import com.taskforge.entity.TeamMember;
import com.taskforge.service.ProjectService;
import com.taskforge.service.TeamMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;

    public ProjectController(ProjectService projectService, TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Project project) {
        projectService.save(project);
        return new ResponseEntity<>("Project saved successfully", HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public List<Project> getAll() {
        return projectService.getAll();
    }

    // C4: projects of a team member, listed once even with many tasks
    @GetMapping("/of-team-member/{teamMemberId}")
    public ResponseEntity<?> getProjectsOfTeamMember(@PathVariable UUID teamMemberId) {
        Optional<TeamMember> teamMember = teamMemberService.getById(teamMemberId);
        if (teamMember.isEmpty()) {
            return new ResponseEntity<>("The team member with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(projectService.getProjectsOfTeamMember(teamMemberId), HttpStatus.OK);
    }

    // B1: projects of one client, case does not matter, sorted A to Z
    @GetMapping("/by-client-name")
    public List<Project> getByClientName(@RequestParam String clientName) {
        return projectService.getByClientName(clientName);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Optional<Project> project = projectService.getById(id);
        if (project.isPresent()) {
            return new ResponseEntity<>(project.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The project with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Project project) {
        Optional<Project> updated = projectService.update(id, project);
        if (updated.isPresent()) {
            return new ResponseEntity<>(updated.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The project with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        if (projectService.deleteById(id)) {
            return new ResponseEntity<>("Project deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("The project with that id does not exist", HttpStatus.NOT_FOUND);
    }
}
