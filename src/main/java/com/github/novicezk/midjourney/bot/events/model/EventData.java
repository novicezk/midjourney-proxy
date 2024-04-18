package com.github.novicezk.midjourney.bot.events.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class EventData {
    private String action;
    private String userId;
    private Timestamp timestamp;
    private int id;

    public EventData(int id, String action, String userId, Timestamp timestamp) {
        this.action = action;
        this.userId = userId;
        this.timestamp = timestamp;
        this.id = id;
    }
}
