package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_email", nullable = false)
    private String to;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "created_at")
    private LocalDateTime date;

    // Required by JPA
    public Notification() {}

    // Builder constructor
    @Builder
    public Notification(
            Long id,
            String to,
            String message,
            boolean read,
            NotificationType type,
            LocalDateTime date) {

        this.id = id;
        this.to = to;
        this.message = message;
        this.read = read;
        this.type = type;
        this.date = date;
    }

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();

        if (type == null) {
            type = NotificationType.INFO;
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public enum NotificationType {
        INFO,
        TASK_ASSIGNED,
        TASK_UPDATED,
        COMMENT,
        DEADLINE,
        HELP_REQUEST,
        CHAT_MESSAGE
    }
}