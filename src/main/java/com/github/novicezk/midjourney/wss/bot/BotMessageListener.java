package com.github.novicezk.midjourney.wss.bot;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BotMessageListener extends ListenerAdapter {
    @Getter
    private final String channelId;


    private final List<MessageHandler> messageHandlers;


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
        if (!this.channelId.equals(channelId)) {
            return true;
        }
        String authorName = message.getAuthor().getName();
        if (CharSequenceUtil.isBlank(authorName)) {
            authorName = "System";
        }
        log.debug("{} - {} - {}: {}", channelId, messageType.name(), authorName, message.getContentRaw());
        return false;
    }

}
