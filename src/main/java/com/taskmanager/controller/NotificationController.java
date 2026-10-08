package com.taskmanager.controller;

import com.taskmanager.entity.Notification;
import com.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam(required = false) String to) {
        if (to != null && !to.isEmpty()) {
            return ResponseEntity.ok(notificationService.getNotificationsByEmail(to));
        }
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestParam String email) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(email));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(email)));
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String message = body.get("message");
        String type = body.getOrDefault("type", "INFO");

        Notification.NotificationType notifType;
        try {
            notifType = Notification.NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            notifType = Notification.NotificationType.INFO;
        }

        return ResponseEntity.ok(notificationService.createNotification(to, message, notifType));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestParam String email) {
        notificationService.markAllAsRead(email);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
