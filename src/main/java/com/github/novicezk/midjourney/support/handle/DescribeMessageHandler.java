package com.github.novicezk.midjourney.support.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DescribeMessageHandler implements MessageHandler {
	private final TaskService taskService;
	private final NotifyService notifyService;

	@Override
	public void onMessageReceived(Message message) {
	}

	@Override
	public void onMessageUpdate(Message message) {
		List<MessageEmbed> embeds = message.getEmbeds();
		if (embeds.isEmpty()) {
			return;
		}
		String prompt = embeds.get(0).getDescription();
		String imageUrl = embeds.get(0).getImage().getUrl();
		int hashStartIndex = imageUrl.lastIndexOf("/");
		String taskId = CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true);
		Task task = this.taskService.getTask(taskId);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		task.setPrompt(prompt);
		task.setPromptEn(prompt);
		task.setImageUrl(imageUrl);
		task.setFinishTime(System.currentTimeMillis());
		task.setStatus(TaskStatus.SUCCESS);
		task.notifyStatusChange();
		this.taskService.putTask(taskId, task);
		this.notifyService.notifyTaskChange(task);
	}

}
