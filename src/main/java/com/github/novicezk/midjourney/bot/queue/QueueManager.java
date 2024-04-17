package com.github.novicezk.midjourney.bot.queue;

import com.github.novicezk.midjourney.bot.utils.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueManager {
    private static final Map<String, QueueEntry> queueMap = new HashMap<>();

    public static void addToQueue(Guild guild, String prompt, String userId, String taskId, String message) {
        queueMap.put(cleanPrompt(prompt), new QueueEntry(userId, taskId, message, prompt));
        notifyQueueChannel(guild, userId);
    }

    public static QueueEntry removeFromQueue(String prompt) {
        return queueMap.remove(cleanPrompt(prompt));
    }

    public static List<QueueEntry> getCurrentQueue() {
        return new ArrayList<>(queueMap.values());
    }

    public static boolean isUserInQueue(String userId) {
        return queueMap.containsKey(userId);
    }

    public static void clearQueue() {
        queueMap.clear();
    }

    private static void notifyQueueChannel(Guild guild, String userId) {
        TextChannel channel = guild.getTextChannelById(Config.getQueueChannel());
        if (channel != null) {
            channel.sendMessage("<@" + userId + "> you're in the queue at number **" + getCurrentQueue().size() + "**")
                    .queue();
        }
    }

    private static String cleanPrompt(String prompt) {
        // trim links part
        int startIndex = prompt.indexOf("--sref");
        if (startIndex != -1) {
            return prompt.substring(0, startIndex);
        }

        return prompt;
    }
}
