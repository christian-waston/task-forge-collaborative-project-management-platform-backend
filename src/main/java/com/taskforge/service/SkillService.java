package com.taskforge.service;

import com.taskforge.entity.Skill;
import com.taskforge.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public Skill save(Skill skill) {
        return skillRepository.save(skill);
    }

    public List<Skill> getAll() {
        return skillRepository.findAll();
    }

    public Optional<Skill> getById(UUID id) {
        return skillRepository.findById(id);
    }

    public Optional<Skill> update(UUID id, Skill skill) {
        if (!skillRepository.existsById(id)) {
            return Optional.empty();
        }
        skill.setId(id);
        return Optional.of(skillRepository.save(skill));
    }

    public boolean deleteById(UUID id) {
        if (!skillRepository.existsById(id)) {
            return false;
        }
        skillRepository.deleteById(id);
        return true;
    }
}
