package com.taskforge.controller;

import com.taskforge.entity.Task;
import com.taskforge.entity.TaskStatus;
import com.taskforge.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Task task) {
        String message = taskService.save(task);
        if (message.equals("Task date cannot be in the past")) {
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        } else if (message.equals("Team member already has a task due on that date")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public List<Task> getAll() {
        return taskService.getAll();
    }

    @GetMapping("/by-status")
    public List<Task> getByStatus(@RequestParam TaskStatus status) {
        return taskService.getByStatus(status);
    }

    @GetMapping("/between")
    public List<Task> getByDueDateBetween(@RequestParam String start, @RequestParam String end) {
        return taskService.getByDueDateBetween(LocalDate.parse(start), LocalDate.parse(end));
    }

    @GetMapping("/stats/by-status")
    public List<Object[]> getStatsByStatus() {
        return taskService.getStatsByStatus();
    }

    @GetMapping("/page")
    public Page<Task> getTasksPage(@RequestParam int page, @RequestParam int size, @RequestParam String sort) {
        String[] sortParts = sort.split(",");
        Sort sortSpec = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")
                ? Sort.by(sortParts[0]).descending()
                : Sort.by(Sort.Direction.ASC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        return taskService.getPage(pageable);
    }

    @PatchMapping("/cancel-day")
    public ResponseEntity<String> cancelDay(@RequestParam UUID teamMemberId, @RequestParam String date) {
        return new ResponseEntity<>(taskService.cancelDay(teamMemberId, LocalDate.parse(date)), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Optional<Task> task = taskService.getById(id);
        if (task.isPresent()) {
            return new ResponseEntity<>(task.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The task with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Task task) {
        Optional<Task> updated = taskService.update(id, task);
        if (updated.isPresent()) {
            return new ResponseEntity<>(updated.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The task with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = taskService.deleteTask(id);
        if (message.equals("The task with that id does not exist")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        } else if (message.equals("That task has work logs, delete them first")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID id, @RequestParam TaskStatus status) {
        String message = taskService.updateStatus(id, status);
        if (message.equals("The task with that id does not exist")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        } else if (message.startsWith("Cannot change status from")) {
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
