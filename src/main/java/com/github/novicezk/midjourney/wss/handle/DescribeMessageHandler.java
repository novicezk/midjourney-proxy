package com.github.novicezk.midjourney.wss.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DescribeMessageHandler implements MessageHandler {
	private final TaskService taskQueueService;

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
		Task task = this.taskQueueService.getRunningTask(taskId);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		task.setPrompt(prompt);
		task.setPromptEn(prompt);
		task.setImageUrl(imageUrl);
		task.setFinishTime(System.currentTimeMillis());
		task.setStatus(TaskStatus.SUCCESS);
		task.awake();
	}

	@Override
	public void onMessageReceived(DataObject data) {
	}

	@Override
	public void onMessageUpdate(DataObject data) {
		DataArray embeds = data.getArray("embeds");
		if (embeds.isEmpty()) {
			return;
		}
		String prompt = embeds.getObject(0).getString("description");
		String imageUrl = embeds.getObject(0).getObject("image").getString("url");
		int hashStartIndex = imageUrl.lastIndexOf("/");
		String taskId = CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true);
		Task task = this.taskQueueService.getRunningTask(taskId);
		if (task == null) {
			return;
		}
		task.setMessageId(data.getString("id"));
		task.setPrompt(prompt);
		task.setPromptEn(prompt);
		task.setImageUrl(imageUrl);
		task.setFinishTime(System.currentTimeMillis());
		task.setStatus(TaskStatus.SUCCESS);
		task.awake();
	}

}
