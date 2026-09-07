package com.airtribe.tasktrackingsystem.controller;

import com.airtribe.tasktrackingsystem.dto.request.TaskRequest;
import com.airtribe.tasktrackingsystem.dto.response.TaskResponse;
import com.airtribe.tasktrackingsystem.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService  taskService;
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest request,
                                           Authentication authentication) {
        return ResponseEntity.ok(taskService.createTask(request, authentication.getName()));
    }
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            Pageable pageable,
            Authentication authentication){
        return ResponseEntity.ok(taskService.getTask(status, search, sortBy, pageable, authentication.getName));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @Valid @RequestBody TaskRequest request){
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();

    }
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id){
        return ResponseEntity.ok(taskService.completeTask(id));
    }
    @PostMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long taskId,
            @PathVariable Long userId){
        return ResponseEntity.ok(taskService.assignTask(taskId, userId));
    }

}















