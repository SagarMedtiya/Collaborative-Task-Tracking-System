package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.config.TestContainerConfig;
import com.airtribe.tasktrackingsystem.dto.request.TaskRequest;
import com.airtribe.tasktrackingsystem.dto.response.TaskResponse;
import com.airtribe.tasktrackingsystem.model.Task;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.TaskRepository;
import com.airtribe.tasktrackingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
public class TaskServiceTest {
    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private Users testUser;
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new Users();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

    }
    @Test
    void shouldCreateTaskSuccessfully() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setDueDate(LocalDateTime.now().plusDays(1));
        taskRequest.setAssignedToId(testUser.getId());

        TaskResponse savedTask = taskService.createTask(taskRequest, "testUser");
        assertNotNull(savedTask.getId());
        assertEquals("Test Task", savedTask.getTitle());
        assertEquals("PENDING", savedTask.getStatus());
    }

}

























