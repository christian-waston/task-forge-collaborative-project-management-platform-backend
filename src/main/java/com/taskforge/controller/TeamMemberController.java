package com.taskforge.controller;

import com.taskforge.entity.TeamMember;
import com.taskforge.service.TeamMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/team-members")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    public TeamMemberController(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody TeamMember teamMember) {
        teamMemberService.save(teamMember);
        return new ResponseEntity<>("Team member saved successfully", HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public List<TeamMember> getAll() {
        return teamMemberService.getAll();
    }

    @GetMapping("/by-skill")
    public List<TeamMember> getBySkill(@RequestParam String name) {
        return teamMemberService.getBySkillName(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Optional<TeamMember> teamMember = teamMemberService.getById(id);
        if (teamMember.isPresent()) {
            return new ResponseEntity<>(teamMember.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The team member with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody TeamMember teamMember) {
        Optional<TeamMember> updated = teamMemberService.update(id, teamMember);
        if (updated.isPresent()) {
            return new ResponseEntity<>(updated.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The team member with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{teamMemberId}/skills/{skillId}")
    public ResponseEntity<String> addSkill(@PathVariable UUID teamMemberId, @PathVariable UUID skillId) {
        String message = teamMemberService.addSkill(teamMemberId, skillId);
        if (message.equals("The team member with that id does not exist")
                || message.equals("That skill does not exist")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        } else if (message.equals("The team member already has that skill")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PatchMapping("/{teamMemberId}/workstation")
    public ResponseEntity<String> changeWorkstation(@PathVariable UUID teamMemberId,
                                                    @RequestParam int stationNumber) {
        String message = teamMemberService.changeWorkstation(teamMemberId, stationNumber);
        if (message.equals("The team member with that id does not exist")
                || message.equals("The workstation with that stationNumber does not exist")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        } else if (message.equals("That workstation is taken by another team member")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        if (teamMemberService.deleteById(id)) {
            return new ResponseEntity<>("Team member deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("The team member with that id does not exist", HttpStatus.NOT_FOUND);
    }
}
