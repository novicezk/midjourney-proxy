package com.github.novicezk.midjourney.bot.events.model;

import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.bot.events.EventUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.json.JSONObject;

public class ButtonEventData implements MixpanelEventData {
    private static final String EVENT_NAME = "button";

    private final JSONObject data = new JSONObject();
    private final String eventName;

    public ButtonEventData(ButtonInteractionEvent event) {
        eventName = event.getComponentId();

        data.put("user-id", event.getUser().getId());
        data.put("user-name", event.getUser().getName());
        data.put("user-name-global", event.getUser().getGlobalName());
        data.put("name", event.getComponentId());
        data.put("type", EVENT_NAME);
        data.put("event-id", event.getId());
        data.put("channel", event.getChannel().getName());
        data.put("version", SeasonTracker.getCurrentSeasonVersion() + "." + SeasonTracker.getCurrentGenerationCount());
        data.put("season", SeasonTracker.getCurrentSeasonVersion());

        Member member = event.getMember();
        if (member != null) {
            data.put("roles", EventUtil.rolesToString(member.getRoles()));
        }
    }

    @Override
    public String getEventName() {
        return EVENT_NAME + "-" + eventName;
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
