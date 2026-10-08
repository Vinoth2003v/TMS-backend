package com.taskmanager.service;

import com.taskmanager.entity.Notification;
import com.taskmanager.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(String to, String message, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .to(to)
                .message(message)
                .type(type != null ? type : Notification.NotificationType.INFO)
                .read(false)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByEmail(String email) {
        return notificationRepository.findByToOrderByDateDesc(email);
    }

    public List<Notification> getUnreadNotifications(String email) {
        return notificationRepository.findByToAndReadFalseOrderByDateDesc(email);
    }

    public long getUnreadCount(String email) {
        return notificationRepository.countByToAndReadFalse(email);
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(String email) {
        List<Notification> unread = notificationRepository.findByToAndReadFalseOrderByDateDesc(email);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
