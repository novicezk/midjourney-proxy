package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.ConvertUtils;
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
		DataArray embeds = message.getArray("embeds");
		if (embeds.isEmpty()) {
			return;
		}
		DataObject embed = embeds.getObject(0);
		String title = embed.getString("title", null);
		String description = embed.getString("description", null);
		String footerText = "";
		Optional<DataObject> footer = embed.optObject("footer");
		if (footer.isPresent()) {
			footerText = footer.get().getString("text", "");
		}
		if (!CharSequenceUtil.equalsAny(title, "Action needed to continue", "Action required to continue", "Internal error")) {
			log.warn("discord任务失败, {}: {} \n{}", title, description, footerText);
			return;
		}

		Task targetTask = null;
		if (CharSequenceUtil.startWith(footerText, "/imagine ")) {
			String finalPrompt = CharSequenceUtil.subAfter(footerText, "/imagine ", false);
			String taskId = ConvertUtils.findTaskIdByFinalPrompt(finalPrompt);
			targetTask = this.taskService.getRunningTask(taskId);
		} else if (CharSequenceUtil.startWith(footerText, "/describe ")) {
			String imageUrl = CharSequenceUtil.subAfter(footerText, "/describe ", false);
			int hashStartIndex = imageUrl.lastIndexOf("/");
			String taskId = CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true);
			targetTask = this.taskService.getRunningTask(taskId);
		}
		String reason;
		if (CharSequenceUtil.contains(description, "against our community standards")) {
			// 可能包含违规信息
			log.warn("discord任务失败, 可能包含违规信息, {}", footerText);
			reason = "可能包含违规信息";
		} else if (CharSequenceUtil.contains(description, "verify you're human")) {
			// 图片验证码
			log.error("discord任务失败, 需要人工验证, {}", footerText);
			reason = "需要人工验证，请联系管理员";
		} else {
			log.error("discord任务失败, {}: {}\n {}", title, description, footerText);
			reason = description;
		}
		if (targetTask != null) {
			targetTask.fail(reason);
			targetTask.awake();
		}
	}

	@Override
	public void handle(MessageType messageType, Message message) {
		// bot-wss 获取不到错误
	}

}
