package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskTimeoutSchedule {
	private final TaskQueueHelper taskQueueHelper;
	private final ProxyProperties properties;

	@Scheduled(fixedRate = 30000L)
	public void checkTasks() {
		long currentTime = System.currentTimeMillis();
		long timeout = TimeUnit.MINUTES.toMillis(this.properties.getQueue().getTimeoutMinutes());
		this.taskQueueHelper.findRunningTask(new TaskCondition())
				.filter(t -> currentTime - t.getStartTime() > timeout)
				.forEach(task -> {
					if (Set.of(TaskStatus.FAILURE, TaskStatus.SUCCESS).contains(task.getStatus())) {
						log.warn("task status is failure/success but is in the queue, end it. id: {}", task.getId());
					} else {
						log.debug("task timeout, id: {}", task.getId());
						task.fail("任务超时");
					}
					Future<?> future = this.taskQueueHelper.getRunningFuture(task.getId());
					if (future != null) {
						future.cancel(true);
					}
					this.taskQueueHelper.changeStatusAndNotify(task, task.getStatus());
				});
	}
}
