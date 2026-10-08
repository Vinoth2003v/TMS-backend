package com.taskmanager.controller;

import com.taskmanager.entity.ChatMessage;
import com.taskmanager.entity.Notification;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ChatMessageRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        try {
            String messageText = (String) payload.get("message");
            if (messageText == null || messageText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            String receiverEmail = (String) payload.get("receiverEmail");
            if (receiverEmail == null || receiverEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Receiver email is required"));
            }

            String senderEmail = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                senderEmail = authentication.getName();
            }
            if (senderEmail == null && payload.containsKey("senderEmail")) {
                senderEmail = (String) payload.get("senderEmail");
            }

            if (senderEmail == null || senderEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sender email is required"));
            }

            Long taskId = null;
            if (payload.get("taskId") != null) {
                try {
                    taskId = Long.parseLong(payload.get("taskId").toString());
                } catch (NumberFormatException ignored) {}
            }

            ChatMessage chatMessage = ChatMessage.builder()
                    .senderEmail(senderEmail.trim())
                    .receiverEmail(receiverEmail.trim())
                    .message(messageText.trim())
                    .taskId(taskId)
                    .read(false)
                    .build();

            ChatMessage saved = chatMessageRepository.save(chatMessage);

            // Notify receiver
            String senderName = senderEmail;
            Optional<User> senderOpt = userRepository.findByEmail(senderEmail.trim());
            if (senderOpt.isPresent() && senderOpt.get().getName() != null) {
                senderName = senderOpt.get().getName();
            }

            String preview = messageText.length() > 50 ? messageText.substring(0, 47) + "..." : messageText;
            try {
                notificationService.createNotification(
                        receiverEmail.trim(),
                        "💬 " + senderName + ": " + preview,
                        Notification.NotificationType.CHAT_MESSAGE
                );
            } catch (Exception ignored) {}

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(
            @RequestParam String with,
            @RequestParam(required = false) String current,
            Authentication authentication) {
        String currentUserEmail = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            currentUserEmail = authentication.getName();
        }
        if (currentUserEmail == null && current != null && !current.isBlank()) {
            currentUserEmail = current.trim();
        }

        if (currentUserEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current user email is required"));
        }

        List<ChatMessage> conversation = chatMessageRepository.findConversation(currentUserEmail, with.trim());

        // Mark incoming messages as read
        try {
            chatMessageRepository.markAsRead(with.trim(), currentUserEmail);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markAsRead(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        String receiverEmail = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            receiverEmail = authentication.getName();
        }
        if (receiverEmail == null && payload.containsKey("receiverEmail")) {
            receiverEmail = payload.get("receiverEmail");
        }

        String senderEmail = payload.get("senderEmail");
        if (senderEmail != null && receiverEmail != null) {
            try {
                chatMessageRepository.markAsRead(senderEmail.trim(), receiverEmail.trim());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/unread-counts")
    public ResponseEntity<?> getUnreadCounts(
            @RequestParam(required = false) String email,
            Authentication authentication) {
        String userEmail = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            userEmail = authentication.getName();
        }
        if (userEmail == null && email != null) {
            userEmail = email.trim();
        }

        if (userEmail == null) {
            return ResponseEntity.ok(Map.of("total", 0, "bySender", Collections.emptyMap()));
        }

        long total = chatMessageRepository.countTotalUnread(userEmail);
        List<ChatMessage> allMessages = chatMessageRepository.findAllForUser(userEmail);
        Map<String, Long> bySender = new HashMap<>();
        for (ChatMessage m : allMessages) {
            if (!m.isRead() && userEmail.equalsIgnoreCase(m.getReceiverEmail())) {
                bySender.put(m.getSenderEmail(), bySender.getOrDefault(m.getSenderEmail(), 0L) + 1);
            }
        }

        return ResponseEntity.ok(Map.of("total", total, "bySender", bySender));
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecent(
            @RequestParam(required = false) String email,
            Authentication authentication) {
        String userEmail = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            userEmail = authentication.getName();
        }
        if (userEmail == null && email != null) {
            userEmail = email.trim();
        }

        if (userEmail == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ChatMessage> recent = chatMessageRepository.findAllForUser(userEmail);
        return ResponseEntity.ok(recent);
    }
}

