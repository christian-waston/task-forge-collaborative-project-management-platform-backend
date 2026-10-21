package com.taskforge.service;

import com.taskforge.entity.TaskStatus;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.TeamMemberRepository;
import com.taskforge.repository.WorkLogRepository;
import com.taskforge.repository.WorkstationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkstationRepository workstationRepository;
    private final TaskRepository taskRepository;
    private final WorkLogRepository workLogRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TeamMemberRepository teamMemberRepository,
                            WorkstationRepository workstationRepository,
                            TaskRepository taskRepository,
                            WorkLogRepository workLogRepository) {
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.workstationRepository = workstationRepository;
        this.taskRepository = taskRepository;
        this.workLogRepository = workLogRepository;
    }

    // Bonus: one JSON object built from a LinkedHashMap to keep the order
    public Map<String, Long> getSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("projects", projectRepository.count());
        summary.put("teamMembers", teamMemberRepository.count());
        summary.put("freeWorkstations", (long) workstationRepository.findFreeWorkstations().size());
        summary.put("tasksDueToday", taskRepository.countByDueDate(LocalDate.now()));
        summary.put("open", taskRepository.countByStatus(TaskStatus.OPEN));
        summary.put("workLogs", workLogRepository.count());
        return summary;
    }
}
