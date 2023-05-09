package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MessageData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageListener extends ListenerAdapter {
	private final ProxyProperties properties;
	private final TaskService taskService;
	private final NotifyService notifyService;

	private boolean ignoreMessage(Message message) {
		String authorName = message.getAuthor().getName();
		String channelId = message.getChannel().getId();
		return !this.properties.getDiscord().getMjBotName().equals(authorName) || !this.properties.getDiscord().getChannelId().equals(channelId);
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		Message message = event.getMessage();
		if (ignoreMessage(event.getMessage())) {
			return;
		}
		String content = message.getContentRaw();
		log.debug("消息变更: {}", content);
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

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		if (ignoreMessage(event.getMessage())) {
			return;
		}
		String messageId = message.getId();
		String content = message.getContentRaw();
		log.debug("消息接收: {}", content);
		if (MessageType.SLASH_COMMAND.equals(message.getType()) || MessageType.DEFAULT.equals(message.getType())) {
			MessageData messageData = ConvertUtils.matchImagineContent(content);
			if (messageData == null) {
				return;
			}
			// imagine 命令生成的消息: 启动、完成
			String taskId = ConvertUtils.findTaskIdByFinalPrompt(messageData.getPrompt());
			Task task = this.taskService.getTask(taskId);
			if (task == null) {
				return;
			}
			task.setMessageId(messageId);
			if ("Waiting to start".equals(messageData.getStatus())) {
				task.setStatus(TaskStatus.IN_PROGRESS);
			} else {
				finishTask(task, message);
			}
			this.taskService.putTask(taskId, task);
			this.notifyService.notifyTaskChange(task);
		} else if (MessageType.INLINE_REPLY.equals(message.getType()) && message.getReferencedMessage() != null) {
			MessageData messageData = ConvertUtils.matchUVContent(content);
			if (messageData == null) {
				return;
			}
			// uv 变更图片完成后的消息
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
			task.setMessageId(messageId);
			finishTask(task, message);
			this.taskService.putTask(task.getId(), task);
			this.notifyService.notifyTaskChange(task);
		}
	}

	private void finishTask(Task task, Message message) {
		task.setFinishTime(System.currentTimeMillis());
		if (!message.getAttachments().isEmpty()) {
			task.setStatus(TaskStatus.SUCCESS);
			String imageUrl = message.getAttachments().get(0).getUrl();
			task.setImageUrl(imageUrl);
			int hashStartIndex = imageUrl.lastIndexOf("_");
			// .webp or .png
			int hashEndIndex = imageUrl.endsWith(".webp") ? imageUrl.length() - 5 : imageUrl.length() - 4;
			task.setMessageHash(imageUrl.substring(hashStartIndex + 1, hashEndIndex));
		} else {
			task.setStatus(TaskStatus.FAILURE);
		}
	}

}
