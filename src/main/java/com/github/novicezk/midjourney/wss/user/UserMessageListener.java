package com.github.novicezk.midjourney.wss.user;


import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UserMessageListener {

    @Getter
    private final String channelId;

    private final List<MessageHandler> messageHandlers;


    public void onMessage(DataObject raw) {
        MessageType messageType = MessageType.of(raw.getString("t"));
        if (messageType == null || MessageType.DELETE == messageType) {
            return;
        }
        DataObject data = raw.getObject("d");
        if (ignoreAndLogMessage(data, messageType)) {
            return;
        }
        for (MessageHandler messageHandler : this.messageHandlers) {
            messageHandler.handle(messageType, data);
        }
    }

    private boolean ignoreAndLogMessage(DataObject data, MessageType messageType) {
        String channelId = data.getString("channel_id");
        if (!this.channelId.equals(channelId)) {
            return true;
        }
        String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
        log.debug("{} - {} - {}: {}", channelId, messageType.name(), authorName, data.opt("content").orElse(""));
        return false;
    }
}