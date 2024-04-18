package com.github.novicezk.midjourney.bot.events;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class EventsManager {
    public static void onCommand(SlashCommandInteractionEvent event) {
        EventsStorage.logCommandInvocation(event.getName(), event.getUser().getId());
    }

    public static void onButtonClick(ButtonInteractionEvent event) {
        EventsStorage.logButtonInteraction(event.getComponentId(), event.getUser().getId());
    }
}
