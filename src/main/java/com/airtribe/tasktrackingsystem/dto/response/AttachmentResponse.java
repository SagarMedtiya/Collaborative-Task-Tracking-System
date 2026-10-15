package com.airtribe.tasktrackingsystem.dto.response;

import com.airtribe.tasktrackingsystem.model.Attachments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Long taskId;
    private UserResponse.UserSummary uploadedBy;
    private LocalDateTime createdAt;
    public static AttachmentResponse from(Attachments attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .taskId(attachment.getTask().getId())
                .uploadedBy(attachment.getUploadedBy() != null ?
                        new UserResponse.UserSummary(attachment.getUploadedBy().getId(),
                                attachment.getUploadedBy().getUsername(),
                                attachment.getUploadedBy().getEmail()) :
                        null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}



























