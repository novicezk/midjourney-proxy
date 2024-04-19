package com.github.novicezk.midjourney.bot.events;

import com.github.novicezk.midjourney.bot.events.model.ButtonEventData;
import com.github.novicezk.midjourney.bot.events.model.CommandEventData;
import com.github.novicezk.midjourney.bot.events.model.ErrorEventData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class EventsManager {
    public static void onCommand(SlashCommandInteractionEvent event) {
        EventsStorage.logCommandInvocation(event.getName(), event.getUser().getId());
        MixpanelManager.trackEvent(new CommandEventData(event));
    }

    public static void onButtonClick(ButtonInteractionEvent event) {
        EventsStorage.logButtonInteraction(event.getComponentId(), event.getUser().getId());
        MixpanelManager.trackEvent(new ButtonEventData(event));
    }

    public static void onErrorEvent(String userId, String failReason) {
        MixpanelManager.trackEvent(new ErrorEventData(userId, failReason));
    }
}
