package com.github.novicezk.midjourney.task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;

@Component
@EnableScheduling
public class ScheduledTasks {

    @Resource
    private TaskService taskService;

    @Resource
    private ProxyProperties properties;

    @Scheduled(fixedRate = 60000)
    public void checkTasks() {
        long currentTime = System.currentTimeMillis();
        List<Task> tasks = taskService.listTask();
        List<Task> notStartedTasks  = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.SUBMITTED)
                .filter(task -> currentTime - task.getSubmitTime() > TimeUnit.MINUTES.toMillis(properties.getThread().getTimeout()))
                .toList();
        notStartedTasks.forEach(task -> {
            task.notifyStatusChange();
            task.setStatus(TaskStatus.FAILURE);
        });
    }
}
