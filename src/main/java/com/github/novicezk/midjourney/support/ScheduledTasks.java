package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
	private final TaskService taskService;
	private final ProxyProperties properties;

	@Scheduled(fixedRate = 30000L)
	public void checkTasks() {
		long currentTime = System.currentTimeMillis();
		long timeout = TimeUnit.MINUTES.toMillis(this.properties.getQueue().getTimeoutMinutes());
		TaskCondition condition = new TaskCondition()
				.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
		this.taskService.findRunningTask(condition)
				.filter(t -> currentTime - t.getStartTime() > timeout)
				.forEach(task -> {
					task.setFinishTime(System.currentTimeMillis());
					task.setFailReason("任务超时");
					task.setStatus(TaskStatus.FAILURE);
					task.awake();
				});
	}
}
