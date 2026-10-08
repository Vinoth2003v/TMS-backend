package com.taskmanager.controller;

import com.taskmanager.entity.HelpRequest;
import com.taskmanager.entity.HelpRequestMessage;
import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Task;
import com.taskmanager.repository.HelpRequestMessageRepository;
import com.taskmanager.repository.HelpRequestRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/help-requests")
@CrossOrigin(origins = "http://localhost:3000")
public class HelpRequestController {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private HelpRequestMessageRepository helpRequestMessageRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> createHelpRequest(@RequestBody Map<String, Object> payload) {
        try {
            Long taskId = Long.parseLong(payload.get("taskId").toString());
            String issueTitle = (String) payload.get("issueTitle");
            String description = (String) payload.get("description");
            String teamMemberEmail = (String) payload.get("teamMemberEmail");
            String attachmentUrl = (String) payload.get("attachmentUrl");

            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Task not found");
            }

            Task task = taskOpt.get();
            String managerEmail = task.getCreatedBy(); // Assuming creator is the manager

            HelpRequest request = HelpRequest.builder()
                    .task(task)
                    .issueTitle(issueTitle)
                    .description(description)
                    .status("Pending")
                    .teamMemberEmail(teamMemberEmail)
                    .managerEmail(managerEmail)
                    .attachmentUrl(attachmentUrl)
                    .build();

            HelpRequest savedRequest = helpRequestRepository.save(request);

            // Notify Manager
            notificationService.createNotification(managerEmail, "New help request from " + teamMemberEmail + " on task: " + task.getTitle(), Notification.NotificationType.HELP_REQUEST);

            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/member/{email}")
    public ResponseEntity<List<HelpRequest>> getMemberRequests(@PathVariable String email) {
        return ResponseEntity.ok(helpRequestRepository.findByTeamMemberEmail(email));
    }

    @GetMapping("/manager/{email}")
    public ResponseEntity<List<HelpRequest>> getManagerRequests(@PathVariable String email) {
        return ResponseEntity.ok(helpRequestRepository.findByManagerEmail(email));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<HelpRequest> opt = helpRequestRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Not found");

        HelpRequest req = opt.get();
        req.setStatus(payload.get("status"));
        helpRequestRepository.save(req);

        return ResponseEntity.ok(req);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> addMessage(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<HelpRequest> opt = helpRequestRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Not found");

        HelpRequest req = opt.get();
        String text = payload.get("text");
        String senderEmail = payload.get("senderEmail");
        String senderName = payload.get("senderName");

        HelpRequestMessage msg = HelpRequestMessage.builder()
                .helpRequest(req)
                .text(text)
                .senderEmail(senderEmail)
                .senderName(senderName)
                .build();

        helpRequestMessageRepository.save(msg);
        
        // Update status to Responded if manager replies to a Pending request
        if (senderEmail.equals(req.getManagerEmail()) && "Pending".equals(req.getStatus())) {
            req.setStatus("Responded");
            helpRequestRepository.save(req);
        }

        // Notify the other party
        String notifyEmail = senderEmail.equals(req.getManagerEmail()) ? req.getTeamMemberEmail() : req.getManagerEmail();
        notificationService.createNotification(notifyEmail, senderName + " replied to help request: " + req.getIssueTitle(), Notification.NotificationType.INFO);

        return ResponseEntity.ok(msg);
    }
}
