package com.taskmanager.service;

import com.taskmanager.dto.AnalyticsResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public AnalyticsResponse getGlobalAnalytics() {
        List<Task> allTasks = taskRepository.findAll();
        List<User> allUsers = userRepository.findAll();

        long total = allTasks.size();
        long completed = allTasks.stream().filter(t -> "Completed".equals(t.getStatus())).count();
        long todo = allTasks.stream().filter(t -> "Todo".equals(t.getStatus())).count();
        long inProgress = allTasks.stream().filter(t -> "In Progress".equals(t.getStatus())).count();
        long pendingApproval = allTasks.stream().filter(t -> "Pending Approval".equals(t.getStatus())).count();

        Map<String, Long> tasksByStatus = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        Map<String, Long> tasksByPriority = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));

        Map<String, Long> tasksByCategory = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        List<AnalyticsResponse.TeamMemberStats> teamStats = allUsers.stream()
                .map(user -> {
                    long assigned = allTasks.stream()
                            .filter(t -> user.getEmail().equals(t.getAssignedTo()))
                            .count();
                    long completedByUser = allTasks.stream()
                            .filter(t -> user.getEmail().equals(t.getAssignedTo()) && "Completed".equals(t.getStatus()))
                            .count();
                    double productivity = assigned > 0 ? Math.round((double) completedByUser / assigned * 100.0) : 0;

                    return AnalyticsResponse.TeamMemberStats.builder()
                            .name(user.getName())
                            .email(user.getEmail())
                            .assignedTasks(assigned)
                            .completedTasks(completedByUser)
                            .productivity(productivity)
                            .build();
                })
                .filter(s -> s.getAssignedTasks() > 0)
                .sorted(Comparator.comparingDouble(AnalyticsResponse.TeamMemberStats::getProductivity).reversed())
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .totalTasks(total)
                .completedTasks(completed)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .pendingApprovalTasks(pendingApproval)
                .completionRate(total > 0 ? Math.round((double) completed / total * 100.0) : 0)
                .totalUsers((long) allUsers.size())
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .tasksByCategory(tasksByCategory)
                .teamStats(teamStats)
                .build();
    }

    public AnalyticsResponse getManagerAnalytics(String managerEmail) {
        List<Task> managerTasks = taskRepository.findByCreatedBy(managerEmail);
        List<User> allUsers = userRepository.findAll();

        long total = managerTasks.size();
        long completed = managerTasks.stream().filter(t -> "Completed".equals(t.getStatus())).count();
        long todo = managerTasks.stream().filter(t -> "Todo".equals(t.getStatus())).count();
        long inProgress = managerTasks.stream().filter(t -> "In Progress".equals(t.getStatus())).count();
        long pendingApproval = managerTasks.stream().filter(t -> "Pending Approval".equals(t.getStatus())).count();

        Map<String, Long> tasksByStatus = managerTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        Map<String, Long> tasksByPriority = managerTasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));

        Map<String, Long> tasksByCategory = managerTasks.stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        List<AnalyticsResponse.TeamMemberStats> teamStats = allUsers.stream()
                .map(user -> {
                    long assigned = managerTasks.stream()
                            .filter(t -> user.getEmail().equals(t.getAssignedTo()))
                            .count();
                    long completedByUser = managerTasks.stream()
                            .filter(t -> user.getEmail().equals(t.getAssignedTo()) && "Completed".equals(t.getStatus()))
                            .count();
                    double productivity = assigned > 0 ? Math.round((double) completedByUser / assigned * 100.0) : 0;

                    return AnalyticsResponse.TeamMemberStats.builder()
                            .name(user.getName())
                            .email(user.getEmail())
                            .assignedTasks(assigned)
                            .completedTasks(completedByUser)
                            .productivity(productivity)
                            .build();
                })
                .filter(s -> s.getAssignedTasks() > 0)
                .sorted(Comparator.comparingDouble(AnalyticsResponse.TeamMemberStats::getProductivity).reversed())
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .totalTasks(total)
                .completedTasks(completed)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .pendingApprovalTasks(pendingApproval)
                .completionRate(total > 0 ? Math.round((double) completed / total * 100.0) : 0)
                .totalUsers((long) allUsers.size())
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .tasksByCategory(tasksByCategory)
                .teamStats(teamStats)
                .build();
    }
}
