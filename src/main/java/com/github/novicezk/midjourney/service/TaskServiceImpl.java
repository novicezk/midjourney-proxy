package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
	@Resource
	private TaskStoreService taskStoreService;
	@Resource
	private DiscordService discordService;
	@Resource
	private NotifyService notifyService;

	private final ThreadPoolTaskExecutor taskExecutor;
	private final List<Task> runningTasks;

	public TaskServiceImpl(ProxyProperties properties) {
		ProxyProperties.TaskQueueConfig queueConfig = properties.getQueue();
		this.runningTasks = Collections.synchronizedList(new ArrayList<>(queueConfig.getCoreSize() * 2));
		this.taskExecutor = new ThreadPoolTaskExecutor();
		this.taskExecutor.setCorePoolSize(queueConfig.getCoreSize());
		this.taskExecutor.setMaxPoolSize(queueConfig.getCoreSize());
		this.taskExecutor.setQueueCapacity(queueConfig.getQueueSize());
		this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		this.taskExecutor.setThreadNamePrefix("TaskQueue-");
		this.taskExecutor.initialize();
	}

	@Override
	public Task getTask(String id) {
		return this.runningTasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
	}

	@Override
	public Stream<Task> findTask(TaskCondition condition) {
		return this.runningTasks.stream().filter(condition);
	}

	@Override
	public Message<String> submitImagine(Task task) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.imagine(task.getFinalPrompt());
			checkAndWait(task, result);
		});
	}

	@Override
	public Message<String> submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.upscale(targetMessageId, index, targetMessageHash);
			checkAndWait(task, result);
		});
	}

	@Override
	public Message<String> submitVariation(Task task, String targetMessageId, String targetMessageHash, int index) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.variation(targetMessageId, index, targetMessageHash);
			checkAndWait(task, result);
		});
	}

	@Override
	public Message<String> submitDescribe(Task task, DataUrl dataUrl) {
		return submitTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != Message.SUCCESS_CODE) {
				task.setFinishTime(System.currentTimeMillis());
				task.setFailReason(uploadResult.getDescription());
				changeStatusAndNotify(task, TaskStatus.FAILURE);
				return;
			}
			String finalFileName = uploadResult.getResult();
			Message<Void> result = this.discordService.describe(finalFileName);
			checkAndWait(task, result);
		});
	}

	private Message<String> submitTask(Task task, Runnable runnable) {
		this.taskStoreService.saveTask(task);
		int size;
		try {
			size = this.taskExecutor.getThreadPoolExecutor().getQueue().size();
			this.taskExecutor.execute(() -> {
				task.setStartTime(System.currentTimeMillis());
				this.runningTasks.add(task);
				try {
					this.taskStoreService.saveTask(task);
					runnable.run();
				} finally {
					this.runningTasks.remove(task);
				}
			});
		} catch (RejectedExecutionException e) {
			this.taskStoreService.deleteTask(task.getId());
			return Message.failure("队列已满，请稍后尝试");
		}
		if (size == 0) {
			return Message.success(task.getId());
		} else {
			return Message.success(Message.WAITING_CODE, "排队中，前面还有" + size + "个任务", task.getId());
		}
	}

	private void checkAndWait(Task task, Message<Void> result) {
		if (result.getCode() != Message.SUCCESS_CODE) {
			task.setFinishTime(System.currentTimeMillis());
			task.setFailReason(result.getDescription());
			changeStatusAndNotify(task, TaskStatus.FAILURE);
			return;
		}
		changeStatusAndNotify(task, TaskStatus.SUBMITTED);
		do {
			try {
				task.sleep();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			changeStatusAndNotify(task, task.getStatus());
		} while (task.getStatus() == TaskStatus.IN_PROGRESS);
		log.debug("task finished, id: {}, status: {}", task.getId(), task.getStatus());
	}

	private void changeStatusAndNotify(Task task, TaskStatus status) {
		task.setStatus(status);
		this.taskStoreService.saveTask(task);
		this.notifyService.notifyTaskChange(task);
	}

}