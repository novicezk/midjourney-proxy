package com.github.novicezk.midjourney.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
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
	public Task getRunningTask(String id) {
		if (CharSequenceUtil.isBlank(id)) {
			return null;
		}
		return this.runningTasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
	}

	@Override
	public Stream<Task> findRunningTask(TaskCondition condition) {
		return this.runningTasks.stream().filter(condition);
	}

	@Override
	public SubmitResultVO submitImagine(Task task,  DataUrl dataUrl) {
		return submitTask(task, () -> {
			if (dataUrl != null) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					task.fail(uploadResult.getDescription());
					changeStatusAndNotify(task, TaskStatus.FAILURE);
					return;
				}
				String finalFileName = uploadResult.getResult();
				Message<String> sendImageResult = this.discordService.sendImageMessage("upload image: " + finalFileName, finalFileName);
				if (sendImageResult.getCode() != ReturnCode.SUCCESS) {
					task.fail(sendImageResult.getDescription());
					changeStatusAndNotify(task, TaskStatus.FAILURE);
					return;
				}
				task.setPrompt(sendImageResult.getResult() + " " + task.getPrompt());
				task.setPromptEn(sendImageResult.getResult() + " " + task.getPromptEn());
				task.setFinalPrompt("[" + task.getId() + "] " + task.getPromptEn());
				this.taskStoreService.save(task);
			}
			Message<Void> result = this.discordService.imagine(task.getFinalPrompt());
			checkAndWait(task, result);
		});
	}

	@Override
	public SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.upscale(targetMessageId, index, targetMessageHash);
			checkAndWait(task, result);
		});
	}

	@Override
	public SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.variation(targetMessageId, index, targetMessageHash);
			checkAndWait(task, result);
		});
	}

	@Override
	public SubmitResultVO submitDescribe(Task task, DataUrl dataUrl) {
		return submitTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != ReturnCode.SUCCESS) {
				task.fail(uploadResult.getDescription());
				changeStatusAndNotify(task, TaskStatus.FAILURE);
				return;
			}
			String finalFileName = uploadResult.getResult();
			Message<Void> result = this.discordService.describe(finalFileName);
			checkAndWait(task, result);
		});
	}

	@Override
	public SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls) {
		return submitTask(task, () -> {
			List<String> finalFileNames = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					task.fail(uploadResult.getDescription());
					changeStatusAndNotify(task, TaskStatus.FAILURE);
					return;
				}
				finalFileNames.add(uploadResult.getResult());
			}
			Message<Void> result = this.discordService.blend(finalFileNames);
			checkAndWait(task, result);
		});
	}

	private SubmitResultVO submitTask(Task task, Runnable runnable) {
		this.taskStoreService.save(task);
		int size;
		try {
			size = this.taskExecutor.getThreadPoolExecutor().getQueue().size();
			this.taskExecutor.execute(() -> {
				this.runningTasks.add(task);
				try {
					runnable.run();
				} finally {
					this.runningTasks.remove(task);
				}
			});
		} catch (RejectedExecutionException e) {
			this.taskStoreService.delete(task.getId());
			return SubmitResultVO.fail(ReturnCode.QUEUE_REJECTED, "队列已满，请稍后尝试");
		}
		if (size == 0) {
			return SubmitResultVO.of(ReturnCode.SUCCESS, "提交成功", task.getId());
		} else {
			return SubmitResultVO.of(ReturnCode.IN_QUEUE, "排队中，前面还有" + size + "个任务", task.getId())
					.setProperty("numberOfQueues", size);
		}
	}

	private void checkAndWait(Task task, Message<Void> result) {
		if (result.getCode() != ReturnCode.SUCCESS) {
			task.fail(result.getDescription());
			changeStatusAndNotify(task, TaskStatus.FAILURE);
			return;
		}
		task.start();
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
		this.taskStoreService.save(task);
		this.notifyService.notifyTaskChange(task);
	}

}