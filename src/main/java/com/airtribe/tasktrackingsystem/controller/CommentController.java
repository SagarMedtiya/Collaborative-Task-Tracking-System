package com.airtribe.tasktrackingsystem.controller;

import com.airtribe.tasktrackingsystem.dto.request.CommentRequest;
import com.airtribe.tasktrackingsystem.dto.response.CommentResponse;
import com.airtribe.tasktrackingsystem.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
            ){
        return ResponseEntity.ok(commentService.createComment(taskId, request, authentication.getName()));
    }
    @GetMapping
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(commentService.updateComment(commentId, request, authentication.getName()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            Authentication authentication
    ){
        commentService.deleteComment(commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
















