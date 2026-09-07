package com.airtribe.tasktrackingsystem.repository;

import com.airtribe.tasktrackingsystem.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByTaskId(Long taskId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId AND c.parentComment IS NULL")
    Page<Comment> findRootCommentsByTaskId(@Param("taskId") Long taskId, Pageable pageable);


}
