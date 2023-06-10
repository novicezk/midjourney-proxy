package com.github.novicezk.midjourney.wss.bot;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BotMessageListener extends ListenerAdapter implements ApplicationListener<ApplicationStartedEvent> {
	@Resource
	private ProxyProperties properties;
	private final List<MessageHandler> messageHandlers = new ArrayList<>();

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.messageHandlers.addAll(event.getApplicationContext().getBeansOfType(MessageHandler.class).values());
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		if (ignoreAndLogMessage(message, MessageType.CREATE)) {
			return;
		}
		for (MessageHandler messageHandler : this.messageHandlers) {
			messageHandler.handle(MessageType.CREATE, message);
		}
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		Message message = event.getMessage();
		if (ignoreAndLogMessage(message, MessageType.UPDATE)) {
			return;
		}
		for (MessageHandler messageHandler : this.messageHandlers) {
			messageHandler.handle(MessageType.UPDATE, message);
		}
	}

	private boolean ignoreAndLogMessage(Message message, MessageType messageType) {
		String channelId = message.getChannel().getId();
		if (!this.properties.getDiscord().getChannelId().equals(channelId)) {
			return true;
		}
		String authorName = message.getAuthor().getName();
		if (CharSequenceUtil.isBlank(authorName)) {
			authorName = "System";
		}
		log.debug("{} - {}: {}", messageType.name(), authorName, message.getContentRaw());
		return false;
	}

}
