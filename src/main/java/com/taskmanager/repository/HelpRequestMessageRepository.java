package com.taskmanager.repository;

import com.taskmanager.entity.HelpRequestMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpRequestMessageRepository extends JpaRepository<HelpRequestMessage, Long> {
    List<HelpRequestMessage> findByHelpRequestId(Long helpRequestId);
}
