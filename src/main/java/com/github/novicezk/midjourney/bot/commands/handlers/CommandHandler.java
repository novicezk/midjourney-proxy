package com.github.novicezk.midjourney.bot.commands.handlers;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandHandler {
    void handle(SlashCommandInteractionEvent event);
    boolean supports(String eventName);
}
