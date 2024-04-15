package com.github.novicezk.midjourney.bot.queue;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.bot.AdamBotInitializer;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class QueueMessageHandler extends MessageHandler {
    @Override
    public void handle(DiscordInstance instance, MessageType messageType, DataObject message) {
        Guild guild = AdamBotInitializer.getApiInstance().getGuildById(Config.getGuildId());

        String failReason = isError(instance, messageType, message);
        if (failReason != null && guild != null) {
            String userId = message.getObject("interaction_metadata").getString("user_id");
            ErrorMessageHandler.sendMessage(
                    guild, userId, "Critical fail! \uD83C\uDFB2\uD83E\uDD26 Try again or upload new image!", failReason);
        }
    }

    private @Nullable String isError(DiscordInstance instance, MessageType messageType, DataObject message) {
        String content = getMessageContent(message);

        if (content.startsWith("Failed")) {
            return "Started with Failed";
        }

        Optional<DataArray> embedsOptional = message.optArray("embeds");
        if (embedsOptional.isEmpty() || embedsOptional.get().isEmpty()) {
            return null;
        }

        DataObject embed = embedsOptional.get().getObject(0);
        String title = embed.getString("title", null);
        if (title == null || title.isBlank()) {
            return null;
        }

        int color = embed.getInt("color", 0);
        if (color == 16711680) {
            return "**type:** " + embed.getString("type", null) + " - **title:** " + embed.getString("title", null) + " - **desc:** " + embed.getString("description", null);
        }

        String description = embed.getString("description", null);
        if ("link".equals(embed.getString("type", "")) || CharSequenceUtil.isBlank(description)) {
            return null;
        }

        Task task = findTaskWhenError(instance, messageType, message);
        return task != null ? task.getFailReason() : null;
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
