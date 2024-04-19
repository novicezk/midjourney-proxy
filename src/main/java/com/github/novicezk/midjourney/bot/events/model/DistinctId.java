package com.github.novicezk.midjourney.bot.events.model;

public enum DistinctId {
    ACTION("action");

    private final String action;

    DistinctId(String action) {
        this.action = action;
    }

    public String getName() {
        return action;
    }
}
