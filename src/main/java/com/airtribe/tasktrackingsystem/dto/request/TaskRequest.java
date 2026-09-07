package com.airtribe.tasktrackingsystem.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    @Size(min = 1, max= 255, message ="Task title must be between 1 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private Status status;

    private Priority priority;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    private Long assignedToId;

    private Long teamId;

}















