package com.airtribe.tasktrackingsystem.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentSummary {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String authorName;
    public static CommentSummary from(com.airtribe.tasktrackingsystem.model.Comment comment) {
        return CommentSummary.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorName(comment.getUser() != null ? comment.getUser().getFullName(): "Unknown")
                .build();
    }

}













