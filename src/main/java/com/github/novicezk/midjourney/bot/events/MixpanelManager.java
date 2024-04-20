package com.github.novicezk.midjourney.bot.events;

import com.github.novicezk.midjourney.bot.events.model.MixpanelEventData;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.json.JSONObject;

import java.io.IOException;

public class MixpanelManager {
    private static final MixpanelAPI mixpanel = new MixpanelAPI();

    public static void trackEvent(MixpanelEventData data) {
        MessageBuilder messageBuilder = new MessageBuilder(Config.getMixpanelProjectToken());
        JSONObject event = messageBuilder.event(data.getDistinctId(), data.getEventName(), data.getData());
        try {
            mixpanel.sendMessage(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
