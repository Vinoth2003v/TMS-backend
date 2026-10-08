package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadRiskResponse {
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private int activeTasks;
    private int overdueTasks;
    private int highPriorityTasks;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String message;
}
