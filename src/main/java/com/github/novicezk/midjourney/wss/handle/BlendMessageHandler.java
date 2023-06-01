package com.github.novicezk.midjourney.wss.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * blend消息处理.
 * 开始(create): **https://xxx/xxx1/1780749341481612.png https://xxx/xxx2/1780749341481612.png --v 5.1** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **<https://s.mj.run/JWu6jaL1D-8> <https://s.mj.run/QhfnQY-l68o> --v 5.1** - <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **<https://s.mj.run/JWu6jaL1D-8> <https://s.mj.run/QhfnQY-l68o> --v 5.1** - <@1012983546824114217> (relaxed)
 */
@Component
public class BlendMessageHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		Optional<DataObject> interaction = message.optObject("interaction");
		String content = message.getString("content");
		boolean match = CharSequenceUtil.startWith(content, "**<https://s.mj.run/") || (interaction.isPresent() && "blend".equals(interaction.get().getString("name")));
		if (!match) {
			return;
		}
		ContentParseData parseData = parse(content);
		if (parseData == null) {
			return;
		}
		if (MessageType.CREATE == messageType) {
			if ("Waiting to start".equals(parseData.getStatus())) {
				// 开始
				List<String> urls = CharSequenceUtil.split(parseData.getPrompt(), " ");
				if (urls.isEmpty()) {
					return;
				}
				int hashStartIndex = urls.get(0).lastIndexOf("/");
				String taskId = CharSequenceUtil.subBefore(urls.get(0).substring(hashStartIndex + 1), ".", true);
				TaskCondition condition = new TaskCondition()
						.setId(taskId)
						.setActionSet(Set.of(TaskAction.BLEND))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setMessageId(message.getString("id"));
				task.setPrompt(parseData.getPrompt());
				task.setPromptEn(parseData.getPrompt());
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
			} else {
				// 完成
				TaskCondition condition = new TaskCondition()
						.setActionSet(Set.of(TaskAction.BLEND))
						.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
				Task task = this.taskQueueHelper.findRunningTask(condition)
						.max(Comparator.comparing(Task::getProgress))
						.orElse(null);
				if (task == null) {
					return;
				}
				task.setFinalPrompt(parseData.getPrompt());
				finishTask(task, message);
				task.awake();
			}
		} else if (MessageType.UPDATE == messageType) {
			// 进度
			TaskCondition condition = new TaskCondition()
					.setMessageId(message.getString("id"))
					.setActionSet(Set.of(TaskAction.BLEND))
					.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
			if (task == null) {
				return;
			}
			task.setProgress(parseData.getStatus());
			updateTaskImageUrl(task, message);
			task.awake();
		}
	}

	@Override
	public void handle(MessageType messageType, Message message) {
		String content = message.getContentRaw();
		boolean match = CharSequenceUtil.startWith(content, "**<https://s.mj.run/") || (message.getInteraction() != null && "blend".equals(message.getInteraction().getName()));
		if (!match) {
			return;
		}
		ContentParseData parseData = parse(content);
		if (parseData == null) {
			return;
		}
		if (MessageType.CREATE == messageType) {
			if ("Waiting to start".equals(parseData.getStatus())) {
				// 开始
				List<String> urls = CharSequenceUtil.split(parseData.getPrompt(), " ");
				if (urls.isEmpty()) {
					return;
				}
				int hashStartIndex = urls.get(0).lastIndexOf("/");
				String taskId = CharSequenceUtil.subBefore(urls.get(0).substring(hashStartIndex + 1), ".", true);
				TaskCondition condition = new TaskCondition()
						.setId(taskId)
						.setActionSet(Set.of(TaskAction.BLEND))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
				if (task == null) {
					return;
				}
				task.setMessageId(message.getId());
				task.setPrompt(parseData.getPrompt());
				task.setPromptEn(parseData.getPrompt());
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
			} else {
				// 完成
				TaskCondition condition = new TaskCondition()
						.setActionSet(Set.of(TaskAction.BLEND))
						.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
				Task task = this.taskQueueHelper.findRunningTask(condition)
						.max(Comparator.comparing(Task::getProgress))
						.orElse(null);
				if (task == null) {
					return;
				}
				task.setFinalPrompt(parseData.getPrompt());
				finishTask(task, message);
				task.awake();
			}
		} else if (MessageType.UPDATE == messageType) {
			// 进度
			TaskCondition condition = new TaskCondition()
					.setMessageId(message.getId())
					.setActionSet(Set.of(TaskAction.BLEND))
					.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
			Task task = this.taskQueueHelper.findRunningTask(condition).findFirst().orElse(null);
			if (task == null) {
				return;
			}
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
		parseData.setPrompt(matcher.group(1));
		parseData.setStatus(matcher.group(2));
		return parseData;
	}
}
