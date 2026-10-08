package com.taskmanager.controller;

import com.taskmanager.entity.FileAttachment;
import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Task;
import com.taskmanager.repository.FileAttachmentRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload/{taskId}")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            // Create upload directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save attachment record
            FileAttachment attachment = FileAttachment.builder()
                    .fileName(fileName)
                    .originalName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(filePath.toString())
                    .uploadedBy(uploadedBy)
                    .task(task)
                    .build();

            attachment = fileAttachmentRepository.save(attachment);

            // Notify manager that deliverable was uploaded
            if (task.getCreatedBy() != null && !task.getCreatedBy().isBlank()) {
                String by = (uploadedBy != null && !uploadedBy.isBlank()) ? uploadedBy : "Team Member";
                notificationService.createNotification(
                        task.getCreatedBy(),
                        "Team Member " + by + " completed/uploaded deliverable for task '" + task.getTitle() + "': " + file.getOriginalFilename(),
                        Notification.NotificationType.TASK_UPDATED
                );
            }

            return ResponseEntity.ok(Map.of(
                    "id", attachment.getId(),
                    "fileName", attachment.getFileName(),
                    "originalName", attachment.getOriginalName(),
                    "fileType", attachment.getFileType(),
                    "fileSize", attachment.getFileSize(),
                    "message", "File uploaded successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<FileAttachment>> getFilesByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(fileAttachmentRepository.findByTaskId(taskId));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileAttachment attachment = fileAttachmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getOriginalName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            FileAttachment attachment = fileAttachmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Delete physical file
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete record
            fileAttachmentRepository.delete(attachment);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete file"));
        }
    }
}
