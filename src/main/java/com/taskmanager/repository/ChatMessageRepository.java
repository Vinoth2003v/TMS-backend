package com.taskmanager.repository;

import com.taskmanager.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderEmail = :user1 AND m.receiverEmail = :user2) OR " +
           "(m.senderEmail = :user2 AND m.receiverEmail = :user1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.senderEmail = :senderEmail AND m.receiverEmail = :receiverEmail AND m.read = false")
    long countUnreadFromSender(@Param("senderEmail") String senderEmail, @Param("receiverEmail") String receiverEmail);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverEmail = :receiverEmail AND m.read = false")
    long countTotalUnread(@Param("receiverEmail") String receiverEmail);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.read = true WHERE m.senderEmail = :senderEmail AND m.receiverEmail = :receiverEmail AND m.read = false")
    void markAsRead(@Param("senderEmail") String senderEmail, @Param("receiverEmail") String receiverEmail);

    @Query("SELECT m FROM ChatMessage m WHERE m.senderEmail = :email OR m.receiverEmail = :email ORDER BY m.createdAt DESC")
    List<ChatMessage> findAllForUser(@Param("email") String email);
}

