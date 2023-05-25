package com.github.novicezk.midjourney.wss.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

public interface MessageHandler {

	void onMessageReceived(Message message);

	void onMessageUpdate(Message message);

	void onMessageReceived(DataObject data);

	void onMessageUpdate(DataObject data);

	default void finishTask(Task task, Message message) {
		task.setFinishTime(System.currentTimeMillis());
		if (!message.getAttachments().isEmpty()) {
			task.setStatus(TaskStatus.SUCCESS);
			String imageUrl = message.getAttachments().get(0).getUrl();
			task.setImageUrl(imageUrl);
			int hashStartIndex = imageUrl.lastIndexOf("_");
			task.setMessageHash(CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true));
		} else {
			task.setStatus(TaskStatus.FAILURE);
		}
	}

	default void finishTask(Task task, DataObject data) {
		task.setFinishTime(System.currentTimeMillis());
		DataArray attachments = data.getArray("attachments");
		if (!attachments.isEmpty()) {
			task.setStatus(TaskStatus.SUCCESS);
			String imageUrl = attachments.getObject(0).getString("url");
			task.setImageUrl(imageUrl);
			int hashStartIndex = imageUrl.lastIndexOf("_");
			task.setMessageHash(CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true));
		} else {
			task.setStatus(TaskStatus.FAILURE);
		}
	}

}
