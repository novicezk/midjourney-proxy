package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
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
 * upscale消息处理.
 * 开始(create): Upscaling image #1 with **cat** - <@1012983546824114217> (Waiting to start)
 * 进度: 无
 * 完成(create): **cat** - Image #1 <@1012983546824114217>
 * 完成-其他情况(create): **cat** - Upscaled by <@1083152202048217169> (fast)
 */
@Slf4j
@Component
public class UpscaleMessageHandler extends MessageHandler {
	private static final String START_CONTENT_REGEX = "Upscaling image #(\\d) with \\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";
	private static final String END_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Image #(\\d) <@\\d+>";
	private static final String END2_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Upscaled by <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		if (MessageType.CREATE != messageType) {
			return;
		}
		String content = getMessageContent(message);
		UVContentParseData start = parseStart(content);
		if (start != null) {
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(start.getPrompt())
					.setActionSet(Set.of(TaskAction.UPSCALE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED));
			Task task = this.taskQueueHelper.findRunningTask(condition)
					.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "U" + start.getIndex()))
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.awake();
			return;
		}
		UVContentParseData end = parseEnd(content);
		if (end != null) {
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(end.getPrompt())
					.setActionSet(Set.of(TaskAction.UPSCALE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition)
					.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "U" + end.getIndex()))
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
			return;
		}
		UVContentParseData end2 = parseEnd2(content);
		if (end2 != null) {
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(end2.getPrompt())
					.setActionSet(Set.of(TaskAction.UPSCALE))
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

	@Override
	public void handle(MessageType messageType, Message message) {
		if (MessageType.CREATE != messageType) {
			return;
		}
		String content = message.getContentRaw();
		UVContentParseData parseData = parseEnd(content);
		if (parseData != null) {
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(parseData.getPrompt())
					.setActionSet(Set.of(TaskAction.UPSCALE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition)
					.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "U" + parseData.getIndex()))
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
			return;
		}
		UVContentParseData end2 = parseEnd2(content);
		if (end2 != null) {
			TaskCondition condition = new TaskCondition()
					.setFinalPromptEn(end2.getPrompt())
					.setActionSet(Set.of(TaskAction.UPSCALE))
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

	private UVContentParseData parseEnd(String content) {
		Matcher matcher = Pattern.compile(END_CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setPrompt(matcher.group(1));
		parseData.setIndex(Integer.parseInt(matcher.group(2)));
		parseData.setStatus("done");
		return parseData;
	}

	private UVContentParseData parseEnd2(String content) {
		Matcher matcher = Pattern.compile(END2_CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setPrompt(matcher.group(1));
		parseData.setStatus(matcher.group(2));
		return parseData;
	}

}