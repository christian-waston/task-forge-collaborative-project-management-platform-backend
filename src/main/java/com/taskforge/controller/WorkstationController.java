package com.taskforge.controller;

import com.taskforge.entity.Workstation;
import com.taskforge.service.WorkstationService;
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
@RequestMapping("/api/workstations")
public class WorkstationController {

    private final WorkstationService workstationService;

    public WorkstationController(WorkstationService workstationService) {
        this.workstationService = workstationService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Workstation workstation) {
        String message = workstationService.save(workstation);
        if (message.equals("A workstation with that station number already exists")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public List<Workstation> getAll() {
        return workstationService.getAll();
    }

    @GetMapping("/free")
    public List<Workstation> getFree() {
        return workstationService.getFreeWorkstations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Optional<Workstation> workstation = workstationService.getById(id);
        if (workstation.isPresent()) {
            return new ResponseEntity<>(workstation.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The workstation with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Workstation workstation) {
        Optional<Workstation> updated = workstationService.update(id, workstation);
        if (updated.isPresent()) {
            return new ResponseEntity<>(updated.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The workstation with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        if (workstationService.deleteById(id)) {
            return new ResponseEntity<>("Workstation deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("The workstation with that id does not exist", HttpStatus.NOT_FOUND);
    }
}
