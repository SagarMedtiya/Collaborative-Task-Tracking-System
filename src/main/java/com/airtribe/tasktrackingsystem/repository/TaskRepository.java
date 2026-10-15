package com.airtribe.tasktrackingsystem.repository;

import com.airtribe.tasktrackingsystem.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByAssignedToId(Long userId,Pageable pageable);
    Page<Task> findByCreatedById(Long userId, Pageable pageable);
    Page<Task> findByTeamId(Long teamId, Pageable pageable);
    Page<Task> findByAssignedToIdAndStatus(long userId, com.airtribe.tasktrackingsystem.model.Task.Status status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE "+
            "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) AND " +
            "(:teamId IS NULL OR t.team.id = :teamId)")
    Page<Task> searchTasks(@Param("search") String search,
                           @Param("status") com.airtribe.tasktrackingsystem.model.Task.Status status,
                           @Param("assignedToId") Long assignedToId,
                           @Param("teamId") Long teamId,
                           Pageable pageable);
    List<Task> findByDueDateBeforeAndStatusNot(LocalDateTime date, com.airtribe.tasktrackingsystem.model.Task.Status status);
}















