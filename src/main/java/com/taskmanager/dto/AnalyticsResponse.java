package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {
    private long totalTasks;
    private long completedTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long pendingApprovalTasks;
    private double completionRate;
    private long totalUsers;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> tasksByCategory;
    private List<TeamMemberStats> teamStats;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamMemberStats {
        private String name;
        private String email;
        private long assignedTasks;
        private long completedTasks;
        private double productivity;
    }
}
