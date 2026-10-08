package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.Comment;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.taskmanager.entity.Notification;
import com.taskmanager.service.NotificationService;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .assignedTo(request.getAssignedTo())
                .createdBy(request.getCreatedBy())
                .dueDate(request.getDueDate() != null && !request.getDueDate().isEmpty()
                        ? LocalDate.parse(request.getDueDate()) : null)
                .priority(request.getPriority() != null ? request.getPriority() : "Low")
                .status(request.getStatus() != null ? request.getStatus() : "Todo")
                .category(request.getCategory() != null ? request.getCategory() : "Uncategorized")
                .labels(request.getLabels())
                .build();

        task = taskRepository.save(task);
        return toResponse(task);
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return toResponse(task);
    }

    public List<TaskResponse> getTasksByAssignedTo(String email) {
        return taskRepository.findByAssignedTo(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByCreatedBy(String email) {
        return taskRepository.findByCreatedBy(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByUserEmail(String email) {
        return taskRepository.findByUserEmail(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getAssignedTo() != null) task.setAssignedTo(request.getAssignedTo());
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate().isEmpty() ? null : LocalDate.parse(request.getDueDate()));
        }
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) {
            boolean wasNotCompleted = !"Completed".equalsIgnoreCase(task.getStatus());
            task.setStatus(request.getStatus());
            if ("Completed".equalsIgnoreCase(request.getStatus())) {
                task.setCompletedAt(LocalDateTime.now());
                if (request.getCompletedBy() != null) task.setCompletedBy(request.getCompletedBy());
                if (wasNotCompleted && task.getCreatedBy() != null && !task.getCreatedBy().isBlank()) {
                    String by = (request.getCompletedBy() != null && !request.getCompletedBy().isBlank())
                            ? request.getCompletedBy() : "Team Member";
                    notificationService.createNotification(
                            task.getCreatedBy(),
                            "Team Member " + by + " marked task '" + task.getTitle() + "' as Completed",
                            Notification.NotificationType.TASK_UPDATED
                    );
                }
            }
        }
        if (request.getCategory() != null) task.setCategory(request.getCategory());
        if (request.getLabels() != null) task.setLabels(request.getLabels());
        if (request.getCompletedBy() != null) task.setCompletedBy(request.getCompletedBy());

        task = taskRepository.save(task);
        return toResponse(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponse addComment(Long taskId, String text, String authorName, String authorEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Comment comment = Comment.builder()
                .text(text)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .task(task)
                .build();

        commentRepository.save(comment);
        return toResponse(taskRepository.findById(taskId).orElseThrow());
    }

    public List<TaskResponse> getUpcomingDeadlines(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return taskRepository.findUpcomingDeadlines(deadline).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setAssignedTo(task.getAssignedTo());
        response.setCreatedBy(task.getCreatedBy());
        response.setDueDate(task.getDueDate() != null ? task.getDueDate().toString() : null);
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setCategory(task.getCategory());
        response.setLabels(task.getLabels() != null && !task.getLabels().isEmpty()
                ? Arrays.asList(task.getLabels().split(",")) : new ArrayList<>());
        response.setCompletedBy(task.getCompletedBy());
        response.setCompletedAt(task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
        response.setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        response.setUpdatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);

        // Map comments
        List<TaskResponse.CommentDto> commentDtos = new ArrayList<>();
        if (task.getComments() != null) {
            for (Comment c : task.getComments()) {
                TaskResponse.CommentDto dto = new TaskResponse.CommentDto();
                dto.setId(c.getId());
                dto.setText(c.getText());
                dto.setBy(c.getAuthorName());
                dto.setEmail(c.getAuthorEmail());
                dto.setDate(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
                commentDtos.add(dto);
            }
        }
        response.setComments(commentDtos);

        // Map attachments
        List<TaskResponse.AttachmentDto> attachmentDtos = new ArrayList<>();
        if (task.getAttachments() != null) {
            for (var a : task.getAttachments()) {
                TaskResponse.AttachmentDto dto = new TaskResponse.AttachmentDto();
                dto.setId(a.getId());
                dto.setFileName(a.getFileName());
                dto.setOriginalName(a.getOriginalName());
                dto.setFileType(a.getFileType());
                dto.setFileSize(a.getFileSize());
                dto.setUploadedBy(a.getUploadedBy());
                dto.setCreatedAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                attachmentDtos.add(dto);
            }
        }
        response.setAttachments(attachmentDtos);

        return response;
    }
}
