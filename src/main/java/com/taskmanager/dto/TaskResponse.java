package com.taskmanager.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String assignedTo;
    private String createdBy;
    private String dueDate;
    private String priority;
    private String status;
    private String category;
    private List<String> labels;
    private String completedBy;
    private String completedAt;
    private String createdAt;
    private String updatedAt;
    private List<CommentDto> comments;
    private List<AttachmentDto> attachments;

    @Data
    public static class CommentDto {
        private Long id;
        private String text;
        private String by;
        private String email;
        private String date;
    }

    @Data
    public static class AttachmentDto {
        private Long id;
        private String fileName;
        private String originalName;
        private String fileType;
        private Long fileSize;
        private String uploadedBy;
        private String createdAt;
    }
}
