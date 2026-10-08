package com.taskmanager.controller;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final com.taskmanager.repository.UserRepository userRepository;

    private boolean isManager(org.springframework.security.core.Authentication authentication, String createdBy) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            boolean hasManagerRole = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_MANAGER") || a.getAuthority().equalsIgnoreCase("MANAGER"));
            if (hasManagerRole) return true;
            java.util.Optional<com.taskmanager.entity.User> u = userRepository.findByEmail(authentication.getName());
            if (u.isPresent() && u.get().getRole() == com.taskmanager.entity.User.Role.MANAGER) return true;
        }
        if (createdBy != null && !createdBy.isBlank()) {
            java.util.Optional<com.taskmanager.entity.User> u = userRepository.findByEmail(createdBy.trim());
            if (u.isPresent() && u.get().getRole() == com.taskmanager.entity.User.Role.MANAGER) return true;
        }
        return false;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String createdBy) {

        if (assignedTo != null && !assignedTo.isEmpty()) {
            return ResponseEntity.ok(taskService.getTasksByAssignedTo(assignedTo));
        } else if (createdBy != null && !createdBy.isEmpty()) {
            return ResponseEntity.ok(taskService.getTasksByCreatedBy(createdBy));
        }
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTaskById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<TaskResponse>> getTasksByUserEmail(@PathVariable String email) {
        return ResponseEntity.ok(taskService.getTasksByUserEmail(email));
    }

    @GetMapping("/deadlines")
    public ResponseEntity<List<TaskResponse>> getUpcomingDeadlines(
            @RequestParam(defaultValue = "3") int days) {
        return ResponseEntity.ok(taskService.getUpcomingDeadlines(days));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request, org.springframework.security.core.Authentication authentication) {
        if (isManager(authentication, request.getCreatedBy())) {
            String assignedTo = request.getAssignedTo();
            if (assignedTo == null || assignedTo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Managers can assign tasks only to Team Members.",
                    "message", "Managers can assign tasks only to Team Members."
                ));
            }
            java.util.Optional<com.taskmanager.entity.User> assignee = userRepository.findByEmail(assignedTo.trim());
            if (assignee.isEmpty() || assignee.get().getRole() != com.taskmanager.entity.User.Role.TEAM_MEMBER) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Managers can assign tasks only to Team Members.",
                    "message", "Managers can assign tasks only to Team Members."
                ));
            }
        }
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskRequest request, org.springframework.security.core.Authentication authentication) {
        try {
            if (request.getAssignedTo() != null && !request.getAssignedTo().trim().isEmpty()) {
                if (isManager(authentication, request.getCreatedBy())) {
                    java.util.Optional<com.taskmanager.entity.User> assignee = userRepository.findByEmail(request.getAssignedTo().trim());
                    if (assignee.isEmpty() || assignee.get().getRole() != com.taskmanager.entity.User.Role.TEAM_MEMBER) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "Managers can assign tasks only to Team Members.",
                            "message", "Managers can assign tasks only to Team Members."
                        ));
                    }
                }
            }
            return ResponseEntity.ok(taskService.updateTask(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            TaskResponse response = taskService.addComment(
                    id, body.get("text"), body.get("by"), body.get("email"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
