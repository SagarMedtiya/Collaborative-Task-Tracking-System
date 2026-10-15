package com.airtribe.tasktrackingsystem.controller;

import com.airtribe.tasktrackingsystem.dto.response.AttachmentResponse;
import com.airtribe.tasktrackingsystem.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ){
        return ResponseEntity.ok(attachmentService.uploadAttachment(taskId, file, authentication.getName()));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponse> getAttachment(@PathVariable Long attachmentId){
        return ResponseEntity.ok(attachmentService.getAttachment(attachmentId));
    }
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId, Authentication authentication){
        attachmentService.deleteAttachment(attachmentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

}




















