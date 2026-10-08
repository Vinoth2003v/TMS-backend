package com.taskmanager.service;

import com.taskmanager.dto.WorkloadRiskResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final TaskRepository taskRepository;

    public WorkloadRiskResponse calculateWorkload(User targetMember) {
        String email = targetMember.getEmail();
        List<Task> allTasks = taskRepository.findByAssignedTo(email);

        // Filter only incomplete tasks (completed tasks must not be counted)
        List<Task> incompleteTasks = allTasks.stream()
                .filter(t -> !isCompletedStatus(t.getStatus()))
                .toList();

        int activeTasks = incompleteTasks.size();

        // Overdue tasks: deadline has passed and task is incomplete
        LocalDate today = LocalDate.now();
        int overdueTasks = (int) incompleteTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .count();

        // High priority / urgent tasks that are incomplete
        int highPriorityTasks = (int) incompleteTasks.stream()
                .filter(t -> isHighOrUrgentPriority(t.getPriority()))
                .count();

        // Transparent risk scoring logic
        String riskLevel;
        if (activeTasks >= 7 || overdueTasks >= 2 || (activeTasks >= 4 && overdueTasks >= 1) || (activeTasks >= 4 && highPriorityTasks >= 2)) {
            riskLevel = "HIGH";
        } else if (activeTasks >= 4 || overdueTasks >= 1 || highPriorityTasks >= 2) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        // Advisory message
        String message;
        if ("HIGH".equals(riskLevel)) {
            if (overdueTasks > 0) {
                message = String.format("⚠️ This member currently has a high workload. They have %d incomplete tasks and %d overdue tasks. Please review before assigning another task.", activeTasks, overdueTasks);
            } else {
                message = String.format("⚠️ This member currently has a high workload. They have %d incomplete tasks. Please review before assigning another task.", activeTasks);
            }
        } else if ("MEDIUM".equals(riskLevel)) {
            message = "⚠️ This member has a moderate workload. Consider reviewing their current tasks.";
        } else {
            message = "✓ This member's current workload is within a normal range.";
        }

        return WorkloadRiskResponse.builder()
                .memberId(targetMember.getId())
                .memberName(targetMember.getName())
                .memberEmail(targetMember.getEmail())
                .activeTasks(activeTasks)
                .overdueTasks(overdueTasks)
                .highPriorityTasks(highPriorityTasks)
                .riskLevel(riskLevel)
                .message(message)
                .build();
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return s.equals("COMPLETED");
    }

    private boolean isHighOrUrgentPriority(String priority) {
        if (priority == null) return false;
        String p = priority.trim().toUpperCase();
        return p.equals("HIGH") || p.equals("URGENT");
    }
}
