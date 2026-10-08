package com.taskmanager.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private String assignedTo;
    private String dueDate;
    private String priority;
    private String status;
    private String createdBy;
    private String category;
    private String labels;
    private String completedBy;
}
