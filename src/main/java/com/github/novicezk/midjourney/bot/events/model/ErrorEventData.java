package com.github.novicezk.midjourney.bot.events.model;

import org.json.JSONObject;

public class ErrorEventData implements MixpanelEventData {
    private static final String EVENT_NAME = "error";

    private final JSONObject data = new JSONObject();

    public ErrorEventData(String userId, String failReason) {
        data.put("user-id", userId);
        data.put("fail-reason", failReason);
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public String getDistinctId() {
        return DistinctId.ACTION.getName();
    }

    @Override
    public JSONObject getData() {
        return data;
    }
}
