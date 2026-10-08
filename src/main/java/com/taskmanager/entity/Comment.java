package com.taskmanager.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Required by JPA
    public Comment() {
    }

    // Builder constructor
    @Builder
    public Comment(
            Long id,
            String text,
            String authorName,
            String authorEmail,
            Task task,
            LocalDateTime createdAt) {

        this.id = id;
        this.text = text;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.task = task;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public Task getTask() {
        return task;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAuthorName(String name) {
        this.authorName = name;
    }

    public void setAuthorEmail(String email) {
        this.authorEmail = email;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setCreatedAt(LocalDateTime t) {
        this.createdAt = t;
    }
}