package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "help_requests")
public class HelpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "issue_title", nullable = false)
    private String issueTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String status; // Pending, Responded, Resolved

    @Column(name = "team_member_email", nullable = false)
    private String teamMemberEmail;

    @Column(name = "manager_email", nullable = false)
    private String managerEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @OneToMany(mappedBy = "helpRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HelpRequestMessage> messages = new ArrayList<>();

    public HelpRequest() {}

    @Builder
    public HelpRequest(Long id, Task task, String issueTitle, String description, String status, String teamMemberEmail, String managerEmail, LocalDateTime createdAt, LocalDateTime updatedAt, String attachmentUrl, List<HelpRequestMessage> messages) {
        this.id = id;
        this.task = task;
        this.issueTitle = issueTitle;
        this.description = description;
        this.status = status;
        this.teamMemberEmail = teamMemberEmail;
        this.managerEmail = managerEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attachmentUrl = attachmentUrl;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "Pending";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Task getTask() { return task; }
    public String getIssueTitle() { return issueTitle; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getTeamMemberEmail() { return teamMemberEmail; }
    public String getManagerEmail() { return managerEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public List<HelpRequestMessage> getMessages() { return messages; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTask(Task task) { this.task = task; }
    public void setIssueTitle(String issueTitle) { this.issueTitle = issueTitle; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setTeamMemberEmail(String teamMemberEmail) { this.teamMemberEmail = teamMemberEmail; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public void setMessages(List<HelpRequestMessage> messages) { this.messages = messages; }
}
