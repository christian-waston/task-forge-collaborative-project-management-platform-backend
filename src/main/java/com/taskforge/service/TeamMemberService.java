package com.taskforge.service;

import com.taskforge.entity.Skill;
import com.taskforge.entity.TeamMember;
import com.taskforge.entity.Workstation;
import com.taskforge.repository.SkillRepository;
import com.taskforge.repository.TeamMemberRepository;
import com.taskforge.repository.WorkstationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final WorkstationRepository workstationRepository;
    private final SkillRepository skillRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository,
                             WorkstationRepository workstationRepository,
                             SkillRepository skillRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.workstationRepository = workstationRepository;
        this.skillRepository = skillRepository;
    }

    public TeamMember save(TeamMember teamMember) {
        // The workstation and the skills arrive as detached references with only their id,
        // so load the managed versions before saving the team member.
        if (teamMember.getWorkstation() != null && teamMember.getWorkstation().getId() != null) {
            Optional<Workstation> workstation =
                    workstationRepository.findById(teamMember.getWorkstation().getId());
            workstation.ifPresent(teamMember::setWorkstation);
        }
        if (teamMember.getSkills() != null && !teamMember.getSkills().isEmpty()) {
            List<Skill> managedSkills = new ArrayList<>();
            for (Skill skill : teamMember.getSkills()) {
                skillRepository.findById(skill.getId()).ifPresent(managedSkills::add);
            }
            teamMember.setSkills(managedSkills);
        }
        return teamMemberRepository.save(teamMember);
    }

    public List<TeamMember> getAll() {
        return teamMemberRepository.findAll();
    }

    // C1: team members holding a skill, matched without regard to letter case
    public List<TeamMember> getBySkillName(String name) {
        return teamMemberRepository.findBySkillName(name);
    }

    public Optional<TeamMember> getById(UUID id) {
        return teamMemberRepository.findById(id);
    }

    public Optional<TeamMember> update(UUID id, TeamMember teamMember) {
        if (!teamMemberRepository.existsById(id)) {
            return Optional.empty();
        }
        teamMember.setId(id);
        return Optional.of(save(teamMember));
    }

    public boolean deleteById(UUID id) {
        if (!teamMemberRepository.existsById(id)) {
            return false;
        }
        teamMemberRepository.deleteById(id);
        return true;
    }
}
