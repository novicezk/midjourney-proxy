package com.github.novicezk.midjourney.bot.queue;

public class QueueEntry {
    private String userId;
    private String taskId;
    private String message;
    private String prompt;
    private int queueIndex;

    public QueueEntry(int queueIndex, String userId, String taskId, String message, String prompt) {
        this.userId = userId;
        this.taskId = taskId;
        this.message = message;
        this.prompt = prompt;
        this.queueIndex = queueIndex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(int queueIndex) {
        this.queueIndex = queueIndex;
    }
}
