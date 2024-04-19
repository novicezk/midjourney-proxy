package com.github.novicezk.midjourney.bot.events.model;

import org.json.JSONObject;

public interface MixpanelEventData {
    String getEventName();
    String getDistinctId();
    JSONObject getData();
}
