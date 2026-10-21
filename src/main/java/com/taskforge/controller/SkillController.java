package com.taskforge.controller;

import com.taskforge.entity.Skill;
import com.taskforge.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Skill skill) {
        skillService.save(skill);
        return new ResponseEntity<>("Skill saved successfully", HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public List<Skill> getAll() {
        return skillService.getAll();
    }

    // C3: skills that no team member holds
    @GetMapping("/unused")
    public List<Skill> getUnused() {
        return skillService.getUnusedSkills();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Optional<Skill> skill = skillService.getById(id);
        if (skill.isPresent()) {
            return new ResponseEntity<>(skill.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The skill with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Skill skill) {
        Optional<Skill> updated = skillService.update(id, skill);
        if (updated.isPresent()) {
            return new ResponseEntity<>(updated.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The skill with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        if (skillService.deleteById(id)) {
            return new ResponseEntity<>("Skill deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("The skill with that id does not exist", HttpStatus.NOT_FOUND);
    }
}
