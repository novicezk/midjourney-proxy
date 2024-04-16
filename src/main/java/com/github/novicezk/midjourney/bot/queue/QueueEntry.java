package com.github.novicezk.midjourney.bot.queue;

public class QueueEntry {
    private String userId;
    private String taskId;
    private String message;
    private String prompt;

    public QueueEntry(String userId, String taskId, String message, String prompt) {
        this.userId = userId;
        this.taskId = taskId;
        this.message = message;
        this.prompt = prompt;
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
}
