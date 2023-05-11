package com.github.novicezk.midjourney.support.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MessageData;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UVMessageHandler implements MessageHandler {
	private final TaskService taskService;
	private final NotifyService notifyService;

	@Override
	public void onMessageReceived(Message message) {
		MessageData messageData = ConvertUtils.matchUVContent(message.getContentRaw());
		if (messageData == null) {
			return;
		}
		TaskCondition condition = new TaskCondition()
				.setKey(message.getReferencedMessage().getId() + "-" + messageData.getAction())
				.setStatusSet(Set.of(TaskStatus.IN_PROGRESS, TaskStatus.NOT_START));
		Task task = this.taskService.listTask().stream()
				.filter(condition)
				.max(Comparator.comparing(Task::getSubmitTime))
				.orElse(null);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		finishTask(task, message);
		this.taskService.putTask(task.getId(), task);
		this.notifyService.notifyTaskChange(task);
	}

	@Override
	public void onMessageUpdate(Message message) {
		String content = message.getContentRaw();
		MessageData data = ConvertUtils.matchImagineContent(content);
		if (data == null) {
			data = ConvertUtils.matchUVContent(content);
		}
		if (data == null) {
			return;
		}
		String relatedTaskId = ConvertUtils.findTaskIdByFinalPrompt(data.getPrompt());
		if (CharSequenceUtil.isBlank(relatedTaskId)) {
			return;
		}
		TaskCondition condition = new TaskCondition()
				.setActionSet(Set.of(Action.UPSCALE, Action.VARIATION))
				.setRelatedTaskId(relatedTaskId)
				.setStatusSet(Set.of(TaskStatus.NOT_START));
		Task task = this.taskService.listTask().stream()
				.filter(condition)
				.max(Comparator.comparing(Task::getSubmitTime))
				.orElse(null);
		if (task == null) {
			return;
		}
		task.setStatus(TaskStatus.IN_PROGRESS);
		this.taskService.putTask(task.getKey(), task);
		this.notifyService.notifyTaskChange(task);
	}

}
