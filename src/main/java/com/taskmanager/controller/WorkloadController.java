package com.taskmanager.controller;

import com.taskmanager.dto.WorkloadRiskResponse;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class WorkloadController {

    private final WorkloadService workloadService;
    private final UserRepository userRepository;

    @GetMapping("/team-members/{memberIdentifier}/workload")
    public ResponseEntity<?> getTeamMemberWorkload(
            @PathVariable String memberIdentifier,
            Authentication authentication) {

        // 1. Verify caller authentication
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        Optional<User> callerOpt = userRepository.findByEmail(authentication.getName());
        if (callerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }

        User caller = callerOpt.get();

        // 2. Only Managers and Admins can access workload analysis
        boolean isManagerOrAdmin = caller.getRole() == User.Role.MANAGER || caller.getRole() == User.Role.ADMIN;
        if (!isManagerOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: Only Managers can view workload risk analysis"));
        }

        // 3. Find target member by ID or by email
        Optional<User> targetMemberOpt = Optional.empty();
        try {
            Long memberId = Long.parseLong(memberIdentifier);
            targetMemberOpt = userRepository.findById(memberId);
        } catch (NumberFormatException e) {
            targetMemberOpt = userRepository.findByEmail(memberIdentifier.trim());
        }

        if (targetMemberOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Team member not found"));
        }

        User targetMember = targetMemberOpt.get();

        // 4. Verify target user is a TEAM_MEMBER (cannot inspect Admins or other Managers)
        if (targetMember.getRole() != User.Role.TEAM_MEMBER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access denied: Workload risk analysis is only available for Team Members"
            ));
        }

        // 5. If caller is a Manager (not Admin), prevent inspecting team members in completely different departments
        if (caller.getRole() == User.Role.MANAGER) {
            if (caller.getDepartment() != null && !caller.getDepartment().isBlank()
                    && targetMember.getDepartment() != null && !targetMember.getDepartment().isBlank()
                    && !caller.getDepartment().equalsIgnoreCase(targetMember.getDepartment())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Access denied: Team member does not belong to your department"
                ));
            }
        }

        // 6. Calculate workload risk analysis
        WorkloadRiskResponse response = workloadService.calculateWorkload(targetMember);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workload")
    public ResponseEntity<?> getWorkloadByEmail(
            @RequestParam String email,
            Authentication authentication) {
        return getTeamMemberWorkload(email, authentication);
    }
}
