package com.github.novicezk.midjourney.wss.user;


import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.wss.handle.DescribeMessageHandler;
import com.github.novicezk.midjourney.wss.handle.ImagineMessageHandler;
import com.github.novicezk.midjourney.wss.handle.UVMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessageListener {
	private final ProxyProperties properties;
	private final ImagineMessageHandler imagineMessageHandler;
	private final UVMessageHandler uvMessageHandler;
	private final DescribeMessageHandler describeMessageHandler;

	public void handle(DataObject raw) {
		String type = raw.getString("t");
		if (!Set.of("MESSAGE_CREATE", "MESSAGE_UPDATE", "MESSAGE_DELETE").contains(type)) {
			return;
		}
		DataObject data = raw.getObject("d");
		if (ignoreAndLogMessage(data, type)) {
			return;
		}
		if ("MESSAGE_CREATE".equals(type)) {
			int mjType = data.getInt("type", -1);
			if (MessageType.SLASH_COMMAND.getId() == mjType || MessageType.DEFAULT.getId() == mjType) {
				this.imagineMessageHandler.onMessageReceived(data);
			} else if (MessageType.INLINE_REPLY.getId() == mjType && data.hasKey("referenced_message")) {
				this.uvMessageHandler.onMessageReceived(data);
			}
		} else if ("MESSAGE_UPDATE".equals(type)) {
			Optional<DataObject> interaction = data.optObject("interaction");
			if (interaction.isPresent() && "describe".equals(interaction.get().getString("name"))) {
				this.describeMessageHandler.onMessageUpdate(data);
			} else {
				this.uvMessageHandler.onMessageUpdate(data);
			}
		}
	}

	private boolean ignoreAndLogMessage(DataObject data, String eventName) {
		String channelId = data.getString("channel_id");
		if (!this.properties.getDiscord().getChannelId().equals(channelId)) {
			return true;
		}
		Optional<DataObject> author = data.optObject("author");
		if (author.isEmpty()) {
			return true;
		}
		String authorName = author.get().getString("username");
		log.debug("{} - {}: {}", eventName, authorName, data.getString("content"));
		return !this.properties.getDiscord().getMjBotName().equals(authorName);
	}
}