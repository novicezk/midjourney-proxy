package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Stream;

@Slf4j
@Component
public class TaskQueueHelper {
	@Resource
	private TaskStoreService taskStoreService;
	@Resource
	private NotifyService notifyService;

	private final ThreadPoolTaskExecutor taskExecutor;
	private final List<Task> runningTasks;
	private final Map<String, Future<?>> taskFutureMap = Collections.synchronizedMap(new HashMap<>());

	public TaskQueueHelper(ProxyProperties properties) {
		ProxyProperties.TaskQueueConfig queueConfig = properties.getQueue();
		this.runningTasks = Collections.synchronizedList(new ArrayList<>(queueConfig.getCoreSize() * 2));
		this.taskExecutor = new ThreadPoolTaskExecutor();
		this.taskExecutor.setCorePoolSize(queueConfig.getCoreSize());
		this.taskExecutor.setMaxPoolSize(queueConfig.getCoreSize());
		this.taskExecutor.setQueueCapacity(queueConfig.getQueueSize());
		this.taskExecutor.setThreadNamePrefix("TaskQueue-");
		this.taskExecutor.initialize();
	}

	public Set<String> getQueueTaskIds() {
		return this.taskFutureMap.keySet();
	}

	public Task getRunningTask(String id) {
		if (CharSequenceUtil.isBlank(id)) {
			return null;
		}
		return this.runningTasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
	}

	public Stream<Task> findRunningTask(TaskCondition condition) {
		return this.runningTasks.stream().filter(condition);
	}

	public Future<?> getRunningFuture(String taskId) {
		return this.taskFutureMap.get(taskId);
	}

	public SubmitResultVO submitTask(Task task, Callable<Message<Void>> discordSubmit) {
		this.taskStoreService.save(task);
		int size;
		try {
			size = this.taskExecutor.getThreadPoolExecutor().getQueue().size();
			Future<?> future = this.taskExecutor.submit(() -> executeTask(task, discordSubmit));
			this.taskFutureMap.put(task.getId(), future);
		} catch (RejectedExecutionException e) {
			this.taskStoreService.delete(task.getId());
			return SubmitResultVO.fail(ReturnCode.QUEUE_REJECTED, "队列已满，请稍后尝试");
		} catch (Exception e) {
			log.error("submit task error", e);
			return SubmitResultVO.fail(ReturnCode.FAILURE, "提交失败，系统异常");
		}
		if (size == 0) {
			return SubmitResultVO.of(ReturnCode.SUCCESS, "提交成功", task.getId());
		} else {
			return SubmitResultVO.of(ReturnCode.IN_QUEUE, "排队中，前面还有" + size + "个任务", task.getId())
					.setProperty("numberOfQueues", size);
		}
	}

	private void executeTask(Task task, Callable<Message<Void>> discordSubmit) {
		this.runningTasks.add(task);
		try {
			task.start();
			Message<Void> result = discordSubmit.call();
			if (result.getCode() != ReturnCode.SUCCESS) {
				task.fail(result.getDescription());
				changeStatusAndNotify(task, TaskStatus.FAILURE);
				return;
			}
			changeStatusAndNotify(task, TaskStatus.SUBMITTED);
			do {
				task.sleep();
				changeStatusAndNotify(task, task.getStatus());
			} while (task.getStatus() == TaskStatus.IN_PROGRESS);
			log.debug("task finished, id: {}, status: {}", task.getId(), task.getStatus());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("task execute error", e);
			task.fail("执行错误，系统异常");
			changeStatusAndNotify(task, TaskStatus.FAILURE);
		} finally {
			this.runningTasks.remove(task);
			this.taskFutureMap.remove(task.getId());
		}
	}

	public void changeStatusAndNotify(Task task, TaskStatus status) {
		task.setStatus(status);
		this.taskStoreService.save(task);
		this.notifyService.notifyTaskChange(task);
	}
}