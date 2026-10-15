package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.request.CommentRequest;
import com.airtribe.tasktrackingsystem.dto.response.CommentResponse;
import com.airtribe.tasktrackingsystem.exception.ResourceNotFoundException;
import com.airtribe.tasktrackingsystem.model.Comment;
import com.airtribe.tasktrackingsystem.model.Task;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(Long taskId, CommentRequest request, String username){
        Task task = taskService.getEntityById(taskId);
        Users user = userService.getEntityByUsername(username);
        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .user(user)
                .build();
        if(request.getParentCommentId() != null){
            Comment parentComment = commentRepository.findById(request.getParentCommentId()).orElseThrow(()-> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        comment = commentRepository.save(comment);
        notificationService.createAndSendNotification(
                task.getCreatedBy(),
                "COMMENT_ADDED",
                "New Comment: "+ task.getTitle(),
                "A new comment was added to your task",
                "Comment",
                comment.getId()
        );
        return CommentResponse.from(comment);
    }
    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable){
        return commentRepository.findRootCommentsByTaskId(taskId, pageable)
                .map(CommentResponse::from);
    }
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String username){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Users user = userService.getEntityByUsername(username);
        if(!comment.getUser().getId().equals(user.getId())){
            throw new RuntimeException("You can only update your own comments");
        }
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        return CommentResponse.from(comment);
    }
    @Transactional
    public void deleteComment(Long commentId, String username){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        Users user = userService.getEntityByUsername(username);
        if(!comment.getUser().getId().equals(user.getId())){
            throw new RuntimeException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

}
























