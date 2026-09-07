package com.airtribe.tasktrackingsystem.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentSummary {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String uploadedBy;

    public static AttachmentSummary from (com.airtribe.tasktrackingsystem.model.Attachments attachments) {
        return AttachmentSummary.builder()
                .id(attachments.getId())
                .fileName(attachments.getFileName())
                .mimeType(attachments.getMimeType())
                .uploadedBy(attachments.getUploadedBy() != null ?
                        attachments.getUploadedBy().getFullName() : "Unknown")
                .build();
    }
}














