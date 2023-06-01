package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskQueueHelper;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Resource;

public abstract class MessageHandler {
	@Resource
	protected TaskQueueHelper taskQueueHelper;
	@Resource
	protected DiscordHelper discordHelper;

	public abstract void handle(MessageType messageType, DataObject message);

	public abstract void handle(MessageType messageType, Message message);

	protected void updateTaskImageUrl(Task task, DataObject message) {
		DataArray attachments = message.getArray("attachments");
		if (!attachments.isEmpty()) {
			String imageUrl = attachments.getObject(0).getString("url");
			task.setImageUrl(replaceCdnUrl(imageUrl));
		}
	}

	protected void finishTask(Task task, DataObject message) {
		task.setMessageId(message.getString("id"));
		DataArray attachments = message.getArray("attachments");
		if (!attachments.isEmpty()) {
			String imageUrl = attachments.getObject(0).getString("url");
			task.setImageUrl(replaceCdnUrl(imageUrl));
			int hashStartIndex = imageUrl.lastIndexOf("_");
			task.setMessageHash(CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true));
			task.success();
		} else {
			task.fail("关联图片不存在");
		}
	}

	protected void updateTaskImageUrl(Task task, Message message) {
		if (!message.getAttachments().isEmpty()) {
			String imageUrl = message.getAttachments().get(0).getUrl();
			task.setImageUrl(replaceCdnUrl(imageUrl));
		}
	}

	protected void finishTask(Task task, Message message) {
		task.setMessageId(message.getId());
		if (!message.getAttachments().isEmpty()) {
			String imageUrl = message.getAttachments().get(0).getUrl();
			task.setImageUrl(replaceCdnUrl(imageUrl));
			int hashStartIndex = imageUrl.lastIndexOf("_");
			task.setMessageHash(CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true));
			task.success();
		} else {
			task.fail("关联图片不存在");
		}
	}

	protected String replaceCdnUrl(String imageUrl) {
		if (CharSequenceUtil.isBlank(imageUrl)) {
			return imageUrl;
		}
		String cdn = this.discordHelper.getCdn();
		if (CharSequenceUtil.startWith(imageUrl, cdn)) {
			return imageUrl;
		}
		return CharSequenceUtil.replaceFirst(imageUrl, DiscordHelper.DISCORD_CDN_URL, cdn);
	}

}
