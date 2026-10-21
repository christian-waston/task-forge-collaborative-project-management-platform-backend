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

    public List<TeamMember> getBySkillName(String name) {
        return teamMemberRepository.findBySkillName(name);
    }

    public String addSkill(UUID teamMemberId, UUID skillId) {
        Optional<TeamMember> teamMember = teamMemberRepository.findById(teamMemberId);
        if (teamMember.isEmpty()) {
            return "The team member with that id does not exist";
        }
        Optional<Skill> skill = skillRepository.findById(skillId);
        if (skill.isEmpty()) {
            return "That skill does not exist";
        }
        boolean alreadyHas = teamMember.get().getSkills().stream()
                .anyMatch(s -> s.getId().equals(skillId));
        if (alreadyHas) {
            return "The team member already has that skill";
        }
        teamMember.get().getSkills().add(skill.get());
        teamMemberRepository.save(teamMember.get());
        return "Skill added to the team member";
    }

    public String changeWorkstation(UUID teamMemberId, int stationNumber) {
        Optional<TeamMember> teamMember = teamMemberRepository.findById(teamMemberId);
        if (teamMember.isEmpty()) {
            return "The team member with that id does not exist";
        }
        Optional<Workstation> workstation = workstationRepository.findByStationNumber(stationNumber);
        if (workstation.isEmpty()) {
            return "The workstation with that stationNumber does not exist";
        }
        if (teamMemberRepository.existsByWorkstationAndIdNot(workstation.get(), teamMemberId)) {
            return "That workstation is taken by another team member";
        }
        teamMember.get().setWorkstation(workstation.get());
        teamMemberRepository.save(teamMember.get());
        return "Workstation changed successfully";
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
