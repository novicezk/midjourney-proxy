package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.MjDiscordProperties;
import com.github.novicezk.midjourney.service.WechatProxyNotifyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageListener extends ListenerAdapter {
	private final MjDiscordProperties properties;
	private final MjTaskHelper taskHelper;
	private final WechatProxyNotifyService wechatProxyNotifyService;

	private static final String MJ_I_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@(\\d+)> \\((.*?)\\)";
	private static final String MJ_U_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Image #(\\d) <@(\\d+)>";
	private static final String MJ_V_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - (.*?) by <@(\\d+)> \\((.*?)\\)";

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		User author = event.getAuthor();
		if (!this.properties.getMjBotName().equals(author.getName())) {
			return;
		}
		if (!this.properties.getChannelId().equals(event.getChannel().getId())) {
			return;
		}
		Message message = event.getMessage();
		String content = message.getContentRaw();
		log.info("收到 {} 消息, type: {}, content: {}", author.getName(), message.getType(), content);
		String messageId;
		SplitResult splitResult;
		if (MessageType.INLINE_REPLY.equals(message.getType()) && message.getReferencedMessage() != null) {
			messageId = message.getReferencedMessage().getId();
			splitResult = splitReplyContent(content);
		} else {
			messageId = message.getId();
			splitResult = splitContent(content);
		}
		if (splitResult == null) {
			return;
		}
		String key = splitResult.getPrompt();
		if ("u".equals(splitResult.getAction()) || "v".equals(splitResult.getAction())) {
			key = messageId + "-" + splitResult.getAction();
		}
		MjTask task = this.taskHelper.getTask(key);
		if (task == null) {
			return;
		}
		task.setMessageId(messageId);
		if ("Waiting to start".equals(splitResult.getStatus())) {
			log.info("通知微信代理, 任务提交成功, 消息ID: {}", messageId);
			boolean success = this.wechatProxyNotifyService.notifyCreated(task.getRoom(), task.getUser(), splitResult.getPrompt(), messageId);
			task.setNotifySuccess(success);
		} else if ("relaxed".equals(splitResult.getStatus())) {
			// 通知微信代理，图片已生成
			task.setDone(true);
			task.setDoneDate(new Date());
			List<Message.Attachment> attachments = message.getAttachments();
			if (attachments.isEmpty()) {
				return;
			}
			String imageUrl = attachments.get(0).getUrl();
			task.setImageUrl(imageUrl);
			log.info("通知微信代理, 图片已生成, 消息ID: {}, url: {}", messageId, imageUrl);
			boolean success;
			if ("i".equals(splitResult.getAction())) {
				task.setMessageHash(CharSequenceUtil.subBetween(imageUrl, "__", ".png"));
				success = this.wechatProxyNotifyService.notifyImagine(task.getRoom(), task.getUser(), splitResult.getPrompt(), messageId, imageUrl);
			} else {
				success = this.wechatProxyNotifyService.notifyUp(task.getRoom(), task.getUser(), imageUrl);
			}
			task.setNotifySuccess(success);
		}
	}

	private SplitResult splitContent(String content) {
		Pattern pattern = Pattern.compile(MJ_I_CONTENT_REGEX);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return null;
		}
		SplitResult result = new SplitResult();
		result.setAction("i");
		result.setPrompt(matcher.group(1));
		result.setStatus(matcher.group(3));
		return result;
	}

	private SplitResult splitReplyContent(String content) {
		Pattern pattern = Pattern.compile(MJ_V_CONTENT_REGEX);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return splitUContent(content);
		}
		SplitResult result = new SplitResult();
		result.setPrompt(matcher.group(1));
		String matchAction = matcher.group(2);
		result.setAction(matchAction.startsWith("Variations") ? "v" : "u");
		result.setStatus(matcher.group(4));
		return result;
	}

	private SplitResult splitUContent(String content) {
		Pattern pattern = Pattern.compile(MJ_U_CONTENT_REGEX);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return null;
		}
		SplitResult result = new SplitResult();
		result.setPrompt(matcher.group(1));
		result.setAction("u");
		result.setStatus("relaxed");
		result.setIndex(matcher.group(2));
		return result;
	}

	@Data
	static class SplitResult {
		private String action;
		private String prompt;
		private String status;
		private String index;
	}

}
