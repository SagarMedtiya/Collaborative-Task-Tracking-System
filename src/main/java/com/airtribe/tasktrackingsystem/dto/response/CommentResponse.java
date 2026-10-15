package com.airtribe.tasktrackingsystem.dto.response;

import com.airtribe.tasktrackingsystem.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long taskId;
    private UserResponse.UserSummary author;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .author(comment.getUser() != null ?
                        new UserResponse.UserSummary(comment.getUser().getId(),
                                comment.getUser().getUsername(),
                                comment.getUser().getEmail()) :
                        null)
                .parentCommentId(comment.getParentComment() != null ?
                        comment.getParentComment().getId() :
                        null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}


























