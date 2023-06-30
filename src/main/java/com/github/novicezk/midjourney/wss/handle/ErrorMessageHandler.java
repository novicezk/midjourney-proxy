package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ErrorMessageHandler extends MessageHandler {

	@Override
	public void handle(MessageType messageType, DataObject message) {
		Optional<DataArray> embedsOptional = message.optArray("embeds");
		if (embedsOptional.isEmpty() || embedsOptional.get().isEmpty()) {
			return;
		}
		DataObject embed = embedsOptional.get().getObject(0);
		String title = embed.getString("title", null);
		if (CharSequenceUtil.isBlank(title) || CharSequenceUtil.startWith(title, "Your info - ")) {
			// 排除正常信息.
			return;
		}
		String description = embed.getString("description", null);
		String footerText = "";
		Optional<DataObject> footer = embed.optObject("footer");
		if (footer.isPresent()) {
			footerText = footer.get().getString("text", "");
		}
		log.warn("检测到可能异常的信息: {}\n{}\nfooter: {}", title, description, footerText);
		if (CharSequenceUtil.contains(description, "this job will start")) {
			// mj队列中, 不认为是异常
			return;
		}
		if (CharSequenceUtil.contains(description, "verify you're human")) {
			String reason = "需要人工验证，请联系管理员";
			this.taskQueueHelper.findRunningTask(new TaskCondition()).forEach(task -> {
				task.fail(reason);
				task.awake();
			});
			return;
		}
		Task targetTask = null;
		if (CharSequenceUtil.startWith(footerText, "/imagine ")) {
			String finalPrompt = CharSequenceUtil.subAfter(footerText, "/imagine ", false);
			if (CharSequenceUtil.contains(finalPrompt, "https://")) {
				// 有可能为blend操作
				String taskId = this.discordHelper.findTaskIdWithCdnUrl(finalPrompt.split(" ")[0]);
				if (taskId != null) {
					targetTask = this.taskQueueHelper.getRunningTask(taskId);
				}
			}
			if (targetTask == null) {
				targetTask = this.taskQueueHelper.findRunningTask(t ->
						t.getAction() == TaskAction.IMAGINE && finalPrompt.startsWith(t.getPromptEn()))
						.findFirst().orElse(null);
			}
		} else if (CharSequenceUtil.startWith(footerText, "/describe ")) {
			String imageUrl = CharSequenceUtil.subAfter(footerText, "/describe ", false);
			String taskId = this.discordHelper.findTaskIdWithCdnUrl(imageUrl);
			targetTask = this.taskQueueHelper.getRunningTask(taskId);
		}
		if (targetTask == null) {
			return;
		}
		String reason;
		if (CharSequenceUtil.contains(description, "against our community standards")) {
			reason = "可能包含违规信息";
		} else {
			reason = description;
		}
		targetTask.fail(reason);
		targetTask.awake();
	}

	@Override
	public void handle(MessageType messageType, Message message) {
		// bot-wss 获取不到错误
	}

}
