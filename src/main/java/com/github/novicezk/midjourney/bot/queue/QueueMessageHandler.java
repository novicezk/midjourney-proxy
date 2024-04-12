package com.github.novicezk.midjourney.bot.queue;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.bot.AdamBotInitializer;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class QueueMessageHandler extends MessageHandler {
    @Override
    public void handle(DiscordInstance instance, MessageType messageType, DataObject message) {
        Guild guild = AdamBotInitializer.getApiInstance().getGuildById(Config.getGuildId());
        if (isError(instance, messageType, message) && guild != null) {
            String userId = message.getObject("interaction_metadata").getString("user_id");
            sendFailMessage(guild, userId);
        }

        if (messageType == MessageType.CREATE) {
            if (guild != null) {
                sendResultMessage(instance, messageType, guild);
            }
        }
    }

    private void sendFailMessage(Guild guild, String userId) {
        TextChannel channel = guild.getTextChannelById(Config.getSendingChannel());
        if (channel != null) {
            channel.sendMessage("<@" + userId + "> \n\nCritical fail! \uD83C\uDFB2\uD83E\uDD26 Try again!").queue();
        }
    }

    private void sendResultMessage(DiscordInstance instance, MessageType messageType, Guild guild) {
        TextChannel channel = guild.getTextChannelById(Config.getQueueChannel());
        if (channel != null) {
//            log.debug("queue size: {}", QueueManager.getCurrentQueue().size());
            for (QueueEntry entry: QueueManager.getCurrentQueue()) {
//                log.debug("taskId: {}, userId: {}", entry.getTaskId(), entry.getUserId());
            }
        }
    }

    private boolean isError(DiscordInstance instance, MessageType messageType, DataObject message) {
        String content = getMessageContent(message);

        if (content.startsWith("Failed")) {
            return true;
        }

        Optional<DataArray> embedsOptional = message.optArray("embeds");
        if (embedsOptional.isEmpty() || embedsOptional.get().isEmpty()) {
            return false;
        }

        DataObject embed = embedsOptional.get().getObject(0);
        String title = embed.getString("title", null);
        if (title == null || title.isBlank()) {
            return false;
        }

        int color = embed.getInt("color", 0);
        if (color == 16711680) {
            return true;
        }

        String description = embed.getString("description", null);
        if ("link".equals(embed.getString("type", "")) || CharSequenceUtil.isBlank(description)) {
            return false;
        }

        Task task = findTaskWhenError(instance, messageType, message);
        return task != null;
    }

    private Task findTaskWhenError(DiscordInstance instance, MessageType messageType, DataObject message) {
        String progressMessageId = null;
        if (MessageType.CREATE.equals(messageType)) {
            progressMessageId = getReferenceMessageId(message);
        } else if (MessageType.UPDATE.equals(messageType)) {
            progressMessageId = message.getString("id");
        }
        if (CharSequenceUtil.isBlank(progressMessageId)) {
            return null;
        }
        TaskCondition condition = new TaskCondition().setStatusSet(Set.of(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED))
                .setProgressMessageId(progressMessageId);
        return instance.findRunningTask(condition).findFirst().orElse(null);
    }
}
