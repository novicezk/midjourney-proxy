package com.github.novicezk.midjourney.wss.handle;


import com.github.novicezk.midjourney.Constants;
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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * imagine消息处理.
 * 开始(create): **cat** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **cat** - <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **cat** - <@1012983546824114217> (relaxed)
 */
@Slf4j
@Component
public class ImagineMessageHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		ContentParseData parseData = parse(content);
		if (parseData == null) {
			return;
		}
		String realPrompt = this.discordHelper.getRealPrompt(parseData.getPrompt());
		if (MessageType.CREATE == messageType) {
			if ("Waiting to start".equals(parseData.getStatus())) {
				// 开始
				TaskCondition condition = new TaskCondition()
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(taskPredicate(condition, realPrompt))
						.findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getString("id"));
				task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, parseData.getPrompt());
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
			} else {
				// 完成
				TaskCondition condition = new TaskCondition()
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
				Task task = this.taskQueueHelper.findRunningTask(taskPredicate(condition, realPrompt))
						.findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, parseData.getPrompt());
				finishTask(task, message);
				task.awake();
			}
		} else if (MessageType.UPDATE == messageType) {
			// 进度
			TaskCondition condition = new TaskCondition()
					.setActionSet(Set.of(TaskAction.IMAGINE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(taskPredicate(condition, realPrompt))
					.findFirst().orElse(null);
			if (task == null) {
				return;
			}
			task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getString("id"));
			task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, parseData.getPrompt());
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.setProgress(parseData.getStatus());
			task.setImageUrl(getImageUrl(message));
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
		String realPrompt = this.discordHelper.getRealPrompt(parseData.getPrompt());
		if (MessageType.CREATE == messageType) {
			if ("Waiting to start".equals(parseData.getStatus())) {
				// 开始
				TaskCondition condition = new TaskCondition()
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(taskPredicate(condition, realPrompt))
						.findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getId());
				task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, parseData.getPrompt());
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
			} else {
				// 完成
				TaskCondition condition = new TaskCondition()
						.setActionSet(Set.of(TaskAction.IMAGINE))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
				Task task = this.taskQueueHelper.findRunningTask(taskPredicate(condition, realPrompt))
						.findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, parseData.getPrompt());
				finishTask(task, message);
				task.awake();
			}
		} else if (MessageType.UPDATE == messageType) {
			// 进度
			TaskCondition condition = new TaskCondition()
					.setActionSet(Set.of(TaskAction.IMAGINE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(taskPredicate(condition, realPrompt))
					.findFirst().orElse(null);
			if (task == null) {
				return;
			}
			task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getId());
			task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, parseData.getPrompt());
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.setProgress(parseData.getStatus());
			task.setImageUrl(getImageUrl(message));
			task.awake();
		}
	}

	private Predicate<Task> taskPredicate(TaskCondition condition, String prompt) {
		return condition.and(t -> prompt.startsWith(t.getPromptEn()));
	}

	private ContentParseData parse(String content) {
		Matcher matcher = Pattern.compile(CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		ContentParseData parseData = new ContentParseData();
		parseData.setPrompt(matcher.group(1));
		parseData.setStatus(matcher.group(2));
		return parseData;
	}

}
