package com.taskforge.service;

import com.taskforge.entity.Task;
import com.taskforge.entity.TaskStatus;
import com.taskforge.entity.WorkLog;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.WorkLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final TaskRepository taskRepository;

    public WorkLogService(WorkLogRepository workLogRepository, TaskRepository taskRepository) {
        this.workLogRepository = workLogRepository;
        this.taskRepository = taskRepository;
    }

    // E1: write a work log, only for DONE tasks, loggedDate set to today
    public String save(UUID taskId, WorkLog workLog) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            return "The task with that id does not exist";
        }
        if (task.get().getStatus() != TaskStatus.DONE) {
            return "Work logs are only for DONE tasks";
        }
        workLog.setTask(task.get());
        workLog.setLoggedDate(LocalDate.now());
        workLogRepository.save(workLog);
        return "Work log saved successfully";
    }

    // E2: work log history of a project across all its tasks
    public List<WorkLog> getProjectHistory(String projectCode) {
        return workLogRepository.findByProjectCode(projectCode);
    }

    public List<WorkLog> getAll() {
        return workLogRepository.findAll();
    }

    public String delete(UUID id) {
        if (!workLogRepository.existsById(id)) {
            return "The work log with that id does not exist";
        }
        workLogRepository.deleteById(id);
        return "Work log deleted successfully";
    }
}
