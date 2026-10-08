package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(String assignedTo);
    List<Task> findByCreatedBy(String createdBy);
    List<Task> findByStatus(String status);
    List<Task> findByPriority(String priority);
    List<Task> findByCategory(String category);

    List<Task> findByAssignedToOrCreatedBy(String assignedTo, String createdBy);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :email OR t.createdBy = :email")
    List<Task> findByUserEmail(@Param("email") String email);

    @Query("SELECT t FROM Task t WHERE t.dueDate <= :date AND t.status != 'Completed'")
    List<Task> findUpcomingDeadlines(@Param("date") LocalDate date);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countByPriorityGroup();

    @Query("SELECT t.assignedTo, COUNT(t) FROM Task t WHERE t.status = 'Completed' GROUP BY t.assignedTo")
    List<Object[]> countCompletedByAssignee();

    @Query("SELECT t.category, COUNT(t) FROM Task t GROUP BY t.category")
    List<Object[]> countByCategoryGroup();

    long countByStatus(String status);
    long countByCreatedBy(String createdBy);
    long countByAssignedTo(String assignedTo);
    long countByStatusAndCreatedBy(String status, String createdBy);
}
