package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.UVContentParseData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * variation消息处理. todo 5.2之后V1-4操作返回的index始终为1, 暂时不判断index
 * 开始(create): Making variations for image #1 with prompt **cat** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **cat** - Variations (Strong) by <@1012983546824114217> (0%) (relaxed)
 * 5.2前-进度(update): **cat** - Variations by <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **cat** - Variations (Strong或Subtle) by <@1012983546824114217> (relaxed)
 * 5.2前-完成(create): **cat** - Variations by <@1012983546824114217> (relaxed)
 */
@Slf4j
@Component
public class VariationMessageHandler extends MessageHandler {
	private static final String START_CONTENT_REGEX = "Making variations for image #(\\d) with prompt \\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";
	private static final String OLD_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Variations by <@\\d+> \\((.*?)\\)";
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Variations \\(.*?\\) by <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		if (MessageType.CREATE.equals(messageType)) {
			UVContentParseData start = parseStart(content);
			if (start != null) {
				// 开始
				TaskCondition condition = new TaskCondition()
						.setFinalPromptEn(start.getPrompt())
						.setActionSet(Set.of(TaskAction.VARIATION))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(condition)
						.min(Comparator.comparing(Task::getSubmitTime))
						.orElse(null);
				if (task == null) {
					return;
				}
				task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getString("id"));
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
				return;
			}
			UVContentParseData end = parse(content);
			if (end == null) {
				return;
			}
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(end.getPrompt())
					.setActionSet(Set.of(TaskAction.VARIATION))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition)
					.max(Comparator.comparing(Task::getProgress))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
		} else if (MessageType.UPDATE == messageType) {
			UVContentParseData parseData = parse(content);
			if (parseData == null || CharSequenceUtil.equalsAny(parseData.getStatus(), "relaxed", "fast")) {
				return;
			}
			TaskCondition condition = new TaskCondition()
					.setProgressMessageId(message.getString("id"))
					.setActionSet(Set.of(TaskAction.VARIATION))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition)
					.findFirst().orElse(null);
			if (task == null) {
				return;
			}
			task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getString("id"));
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.setProgress(parseData.getStatus());
			task.setImageUrl(getImageUrl(message));
			task.awake();
		}
	}

	/**
	 * bot-wss模式，取不到执行进度; todo: 同个任务不同变换对应不上.
	 *
	 * @param messageType messageType
	 * @param message     message
	 */
	@Override
	public void handle(MessageType messageType, Message message) {
		String content = message.getContentRaw();
		if (MessageType.CREATE.equals(messageType)) {
			UVContentParseData parseData = parse(content);
			if (parseData == null) {
				return;
			}
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(parseData.getPrompt())
					.setActionSet(Set.of(TaskAction.VARIATION))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition)
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
		}
	}

	private UVContentParseData parseStart(String content) {
		Matcher matcher = Pattern.compile(START_CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setIndex(Integer.parseInt(matcher.group(1)));
		parseData.setPrompt(matcher.group(2));
		parseData.setStatus(matcher.group(3));
		return parseData;
	}

	private UVContentParseData parse(String content) {
		UVContentParseData data = parse(content, CONTENT_REGEX);
		if (data == null) {
			return parse(content, OLD_CONTENT_REGEX);
		}
		return data;
	}

	private UVContentParseData parse(String content, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setPrompt(matcher.group(1));
		parseData.setStatus(matcher.group(2));
		return parseData;
	}
}
