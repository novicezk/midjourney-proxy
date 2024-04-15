package com.github.novicezk.midjourney.bot.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueManager {
    private static final Map<String, QueueEntry> queueMap = new HashMap<>();

    public static void addToQueue(String userId, String taskId, String message) {
        queueMap.put(userId, new QueueEntry(userId, taskId, message));
    }

    public static QueueEntry removeFromQueue(String userId) {
        return queueMap.remove(userId);
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
}
