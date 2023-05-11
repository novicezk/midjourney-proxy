package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.handle.DescribeMessageHandler;
import com.github.novicezk.midjourney.support.handle.ImagineMessageHandler;
import com.github.novicezk.midjourney.support.handle.UVMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageListener extends ListenerAdapter {
	private final ProxyProperties properties;
	private final ImagineMessageHandler imagineMessageHandler;
	private final UVMessageHandler uvMessageHandler;
	private final DescribeMessageHandler describeMessageHandler;

	private boolean ignoreMessage(Message message) {
		String authorName = message.getAuthor().getName();
		String channelId = message.getChannel().getId();
		return !this.properties.getDiscord().getMjBotName().equals(authorName) || !this.properties.getDiscord().getChannelId().equals(channelId);
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		Message message = event.getMessage();
		log.debug("消息变更: {}", message.getContentRaw());
		if (ignoreMessage(event.getMessage())) {
			return;
		}
		if (message.getInteraction() != null && "describe".equals(message.getInteraction().getName())) {
			this.describeMessageHandler.onMessageUpdate(message);
		} else {
			this.uvMessageHandler.onMessageUpdate(message);
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		log.debug("消息接收: {}", message.getContentRaw());
		if (ignoreMessage(event.getMessage())) {
			return;
		}
		if (MessageType.SLASH_COMMAND.equals(message.getType()) || MessageType.DEFAULT.equals(message.getType())) {
			this.imagineMessageHandler.onMessageReceived(message);
		} else if (MessageType.INLINE_REPLY.equals(message.getType()) && message.getReferencedMessage() != null) {
			this.uvMessageHandler.onMessageReceived(message);
		}
	}

}
