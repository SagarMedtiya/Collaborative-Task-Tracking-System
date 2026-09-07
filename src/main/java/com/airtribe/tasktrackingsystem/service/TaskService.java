package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.request.TaskRequest;
import com.airtribe.tasktrackingsystem.dto.response.TaskResponse;
import com.airtribe.tasktrackingsystem.model.Task;
import com.airtribe.tasktrackingsystem.model.Team;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TeamService teamService;

    @Transactional
    public TaskResponse createTask(TaskRequest request, String username){
        Users createdBy = userService.getEntityByUsername(username);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus(): Task.Status.PENDING)
                .priority(request.getPriority() != null ? request.getPriority(): Task.Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .createdBy(createdBy)
                .build();

        if(request.getAssignedToId() != null){
            Users assignedTo = userService.getEntityById(request.getAssignedToId());
            task.setAssignedTo(assignedTo);
        }
        if(request.getTeamId() != null){
            Team team = teamService.getEntityById(request.getTeamId());
            task.setTeam(team);
        }
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }
    public Page<TaskResponse> getTasks(String status, String search, String sortBy,
                                       Pageable pageable, String username) {
        Users user = userService.getEntityByUsername(username);

        Status taskStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                taskStatus = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        // Apply sorting
        Sort sort = Sort.by("createdAt").descending();
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] parts = sortBy.split(",");
            if (parts.length == 2) {
                sort = Sort.by(parts[0]);
                if (parts[1].equalsIgnoreCase("asc")) {
                    sort = sort.ascending();
                } else {
                    sort = sort.descending();
                }
            }
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        if (search != null && !search.isEmpty()) {
            return taskRepository.searchTasks(search, taskStatus, user.getId(), null, sortedPageable)
                    .map(TaskResponse::from);
        } else {
            if (taskStatus != null) {
                return taskRepository.findByAssignedToIdAndStatus(user.getId(), taskStatus, sortedPageable)
                        .map(TaskResponse::from);
            } else {
                return taskRepository.findByAssignedToId(user.getId(), sortedPageable)
                        .map(TaskResponse::from);
            }
        }
    }
    public TaskResponse getTaskById(Long id, TaskRequest request){
        Task task = taskRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found with id: "+ id));
        return TaskResponse.from(task);
    }
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id:" + id));
        if(request.getTitle() != null){
            task.setTitle(request.getTitle());
        }
        if(request.getDescription() != null){
            task.setDescription(request.getDescription());
        }
        if(request.getStatus()!= null){
            Status oldStatus = task.getStatus();
            task.setStatus(request.getStatus());
            if(request.getStatus() == Status.COMPLETED && oldStatus != Task.Status.COMPLETED){
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if(request.getPriority() != null){
            task.setPriority(request.getPriority());
        }
        if(request.getDueDate() != null){
            task.setDueDate(request.getDueDate());
        }
        if(request.getAssignedToId() != null){
            Users assignedTo = userService.getEntityById(request.getAssignedToId());
            task.setAssignedTo(assignedTo);
        }
        if(request.getTeamId() != null){
            Team team = teamService.getEntityById(request.getTeamId());
            task.setTeam(team);
        }
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }
    @Transactional
    public void deleteTask(Long id){
        if(!taskRepository.existsById(id)){
            throw new ResourceNotFoundException("Task not found with id: "+ id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponse completeTask(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found with id: "+ id));
        task.setStatus(Status.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }
    @Transactional
    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Users assignedTo = userService.getEntityById(userId);
        task.setAssignedTo(assignedTo);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDateTime.now(), Status.COMPLETED);
    }

    public Task getEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }
}
