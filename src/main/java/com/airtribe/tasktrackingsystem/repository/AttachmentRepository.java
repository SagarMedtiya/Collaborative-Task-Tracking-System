package com.airtribe.tasktrackingsystem.repository;

import com.airtribe.tasktrackingsystem.model.Attachments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachments, Long> {
    List<Attachments> findByTaskId(Long taskId);
}
