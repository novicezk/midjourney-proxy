package com.github.novicezk.midjourney.support.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MessageData;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImagineMessageHandler implements MessageHandler {
	private final TaskService taskQueueService;

	@Override
	public void onMessageReceived(Message message) {
		MessageData messageData = ConvertUtils.matchImagineContent(message.getContentRaw());
		if (messageData == null) {
			return;
		}
		String taskId = ConvertUtils.findTaskIdByFinalPrompt(messageData.getPrompt());
		if (CharSequenceUtil.isBlank(taskId)) {
			return;
		}
		Task task = this.taskQueueService.getTask(taskId);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		if ("Waiting to start".equals(messageData.getStatus())) {
			task.setStatus(TaskStatus.IN_PROGRESS);
		} else {
			finishTask(task, message);
		}
		task.awake();
	}

	@Override
	public void onMessageUpdate(Message message) {
	}

}
