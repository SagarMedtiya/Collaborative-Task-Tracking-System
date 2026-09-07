package com.airtribe.tasktrackingsystem.dto.response;

import com.airtribe.tasktrackingsystem.dto.summary.AttachmentSummary;
import com.airtribe.tasktrackingsystem.dto.summary.CommentSummary;
import com.airtribe.tasktrackingsystem.dto.summary.TeamSummary;
import com.airtribe.tasktrackingsystem.dto.summary.UserSummary;
import com.airtribe.tasktrackingsystem.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;

    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private UserSummary createdBy;
    private UserSummary assignedTo;
    private TeamSummary team;
    private LocalDateTime updatedAt;
    private List<CommentSummary> comments;
    private List<AttachmentSummary> attachments;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .createdBy(task.getCreatedBy() != null ?
                        new UserSummary(task.getCreatedBy().getId(), task.getCreatedBy().getUsername(), task.getCreatedBy().getEmail()) :
                        null)
                .assignedTo(task.getAssignedTo() != null ?
                        new UserSummary(task.getAssignedTo().getId(), task.getAssignedTo().getUsername(), task.getAssignedTo().getEmail()) :
                        null)
                .team(task.getTeam() != null ?
                        new TeamSummary(task.getTeam().getId(), task.getTeam().getName()) :
                        null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .comments(task.getComments() != null ?
                        task.getComments().stream()
                                .limit(5)
                                .map(CommentSummary::from)
                                .collect(Collectors.toList()) :
                        null)
                .attachments(task.getAttachments() != null ?
                        task.getAttachments().stream()
                                .limit(5)
                                .map(AttachmentSummary::from)
                                .collect(Collectors.toList()) :
                        null)
                .build();

    }
}
















