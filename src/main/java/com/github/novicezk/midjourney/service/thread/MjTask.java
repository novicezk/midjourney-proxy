package com.github.novicezk.midjourney.service.thread;

import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;

import eu.maxschuster.dataurl.DataUrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
public class MjTask implements Runnable {
    private DiscordService discordService;

    private TaskService taskService;

    private Task task;

    private Task targetTask;

    private Integer index;

    private String taskFileName;

    private DataUrl dataUrl;
    private Action action;

    @Override
    public void run() {
        task.setSubmitTime(System.currentTimeMillis());
        task.setStatus(TaskStatus.SUBMITTED);
        Message<Void> result = null;
        switch (action){
            case IMAGINE -> result = this.discordService.imagine(task.getFinalPrompt());
            case UPSCALE -> result = this.discordService.upscale(targetTask.getMessageId(), index, targetTask.getMessageHash());
            case VARIATION -> result = this.discordService.variation(targetTask.getMessageId(), index, targetTask.getMessageHash());
            case DESCRIBE -> {
                Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
                if (uploadResult.getCode() != Message.SUCCESS_CODE) {
                    return;
                }
                String finalFileName = uploadResult.getResult();
                result = this.discordService.describe(finalFileName);
            }
        }
        if (result.getCode() != Message.SUCCESS_CODE) {
            this.taskService.removeTask(task.getId());
            return;
        }
		try {
			task.waitForStatusChange();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		if (task.getStatus() == TaskStatus.FAILURE || task.getStatus() == TaskStatus.SUBMITTED){
			log.debug("Task execution failed, thread successfully exited");
		}else {
			log.debug("Task execution successful, thread successfully exited");
		}
    }
}
