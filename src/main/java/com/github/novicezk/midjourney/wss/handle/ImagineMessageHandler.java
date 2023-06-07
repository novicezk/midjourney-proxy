package com.github.novicezk.midjourney.wss.handle;


import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * imagine消息处理.
 * 开始(create): **[4619231091196848] cat** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **[4619231091196848] cat** - <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **[4619231091196848] cat** - <@1012983546824114217> (relaxed)
 */
@Slf4j
@Component
public class ImagineMessageHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*\\[(\\d+)\\] (.*?)\\*\\* - <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = message.getString("content");
		ContentParseData parseData = parse(content);
		if (parseData == null) {
			return;
		}
		if (MessageType.CREATE == messageType) {
			if ("Waiting to start".equals(parseData.getStatus())) {
				// 开始
				TaskCondition condition = new TaskCondition()
						.setId(parseData.getTaskId())
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
			} else {
				// 完成
				TaskCondition condition = new TaskCondition()
						.setId(parseData.getTaskId())
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
				Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
				if (task == null) {
					return;
				}
				finishTask(task, message);
				task.awake();
			}
		} else if (MessageType.UPDATE == messageType) {
			// 进度
			TaskCondition condition = new TaskCondition()
					.setId(parseData.getTaskId())
					.setActionSet(Set.of(TaskAction.IMAGINE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
			if (task == null) {
				return;
			}
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.setProgress(parseData.getStatus());
			updateTaskImageUrl(task, message);
			task.awake();
		}
	}

	@Override
	public void handle(MessageType messageType, Message message) {
		String content = message.getContentRaw();
		ContentParseData parseData = parse(content);
		if (parseData == null) {
			return;
		}
		if (MessageType.CREATE == messageType) {
			if ("Waiting to start".equals(parseData.getStatus())) {
				// 开始
				TaskCondition condition = new TaskCondition()
						.setId(parseData.getTaskId())
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
			} else {
				// 完成
				TaskCondition condition = new TaskCondition()
						.setId(parseData.getTaskId())
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
				Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
				if (task == null) {
					return;
				}
				finishTask(task, message);
				task.awake();
			}
		} else if (MessageType.UPDATE == messageType) {
			// 进度
			TaskCondition condition = new TaskCondition()
					.setId(parseData.getTaskId())
					.setActionSet(Set.of(TaskAction.IMAGINE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
			if (task == null) {
				return;
			}
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.setProgress(parseData.getStatus());
			updateTaskImageUrl(task, message);
			task.awake();
		}
	}

	private ContentParseData parse(String content) {
		Matcher matcher = Pattern.compile(CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		ContentParseData parseData = new ContentParseData();
		parseData.setTaskId(matcher.group(1));
		parseData.setPrompt(matcher.group(2));
		parseData.setStatus(matcher.group(3));
		return parseData;
	}

}
