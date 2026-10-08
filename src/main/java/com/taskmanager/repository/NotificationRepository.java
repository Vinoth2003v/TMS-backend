package com.taskmanager.repository;

import com.taskmanager.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByToOrderByDateDesc(String to);
    List<Notification> findByToAndReadFalseOrderByDateDesc(String to);
    long countByToAndReadFalse(String to);
}
