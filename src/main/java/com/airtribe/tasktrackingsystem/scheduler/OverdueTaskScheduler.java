package com.airtribe.tasktrackingsystem.scheduler;

import com.airtribe.tasktrackingsystem.model.Task;
import com.airtribe.tasktrackingsystem.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueTaskScheduler {
    private final TaskService taskService;

    @Scheduled(cron ="0 0 8 * * *")
    public void checkOverdueTasks() {
        log.info("Checking overdue tasks");
        List<Task> overdueTasks = taskService.getOverdueTasks();
        for (Task task : overdueTasks) {
            log.warn("Task overdue: {} (Due: {})", task.getTitle(), task.getDueDate());
            //here you could send email notification or push notificartion
        }
        log.info("Found {} overdue taks", overdueTasks.size());
    }
}















