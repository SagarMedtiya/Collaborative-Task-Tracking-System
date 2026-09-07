package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;

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

    @Autowired
    private UserRepository userRepository;
    private User testUser;
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUswername("testUser");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

    }
    @Test
    void shouldCreateTaskSuccessfully() {
        Task task = new Task();
        task.setTitle("Test Task");
        taskl.setDescription("Test Description");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setAssignedTo(testUser);

        Task savedTask = taskService.createTask(task);
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Test Task");
        assertThat(savedTask.getStatus()).isEqualTo(Task.Status.PENDING);
    }

}

























