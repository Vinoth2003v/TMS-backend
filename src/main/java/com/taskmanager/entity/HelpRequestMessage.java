package com.taskmanager.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "help_request_messages")
public class HelpRequestMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "help_request_id", nullable = false)
    @JsonIgnore
    private HelpRequest helpRequest;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public HelpRequestMessage() {}

    @Builder
    public HelpRequestMessage(Long id, HelpRequest helpRequest, String text, String senderName, String senderEmail, LocalDateTime createdAt) {
        this.id = id;
        this.helpRequest = helpRequest;
        this.text = text;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public HelpRequest getHelpRequest() { return helpRequest; }
    public String getText() { return text; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setHelpRequest(HelpRequest helpRequest) { this.helpRequest = helpRequest; }
    public void setText(String text) { this.text = text; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
