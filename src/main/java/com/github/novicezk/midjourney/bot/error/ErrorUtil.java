package com.github.novicezk.midjourney.bot.error;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class ErrorUtil {
    public static @Nullable String isError(
            DiscordInstance instance,
            MessageType messageType,
            DataObject message,
            String content,
            String referenceMessageId
    ) {
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
            return "**title:** " + embed.getString("title", null) + " - **desc:** " + embed.getString("description", null);
        }

        String description = embed.getString("description", null);
        if ("link".equals(embed.getString("type", "")) || CharSequenceUtil.isBlank(description)) {
            return null;
        }

        Task task = findTaskWhenError(instance, messageType, message, referenceMessageId);
        return task != null ? task.getFailReason() : null;
    }

    private static Task findTaskWhenError(
            DiscordInstance instance,
            MessageType messageType,
            DataObject message,
            String referenceMessageId
    ) {
        String progressMessageId = null;
        if (MessageType.CREATE.equals(messageType)) {
            progressMessageId = referenceMessageId;
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
