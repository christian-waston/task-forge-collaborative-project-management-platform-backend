package com.taskforge.service;

import com.taskforge.entity.Workstation;
import com.taskforge.repository.WorkstationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkstationService {

    private final WorkstationRepository workstationRepository;

    public WorkstationService(WorkstationRepository workstationRepository) {
        this.workstationRepository = workstationRepository;
    }

    public Workstation save(Workstation workstation) {
        return workstationRepository.save(workstation);
    }

    public List<Workstation> getAll() {
        return workstationRepository.findAll();
    }

    // C2: workstations no team member occupies
    public List<Workstation> getFreeWorkstations() {
        return workstationRepository.findFreeWorkstations();
    }

    public Optional<Workstation> getById(UUID id) {
        return workstationRepository.findById(id);
    }

    public Optional<Workstation> update(UUID id, Workstation workstation) {
        if (!workstationRepository.existsById(id)) {
            return Optional.empty();
        }
        workstation.setId(id);
        return Optional.of(workstationRepository.save(workstation));
    }

    public boolean deleteById(UUID id) {
        if (!workstationRepository.existsById(id)) {
            return false;
        }
        workstationRepository.deleteById(id);
        return true;
    }
}
