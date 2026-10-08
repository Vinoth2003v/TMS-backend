package com.taskmanager.repository;

import com.taskmanager.entity.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    List<HelpRequest> findByTeamMemberEmail(String email);
    List<HelpRequest> findByManagerEmail(String email);
    List<HelpRequest> findByTaskId(Long taskId);
}
