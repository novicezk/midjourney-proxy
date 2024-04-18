package com.github.novicezk.midjourney.bot.commands.handlers;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Random;

public class PingCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "ping";
    private static final Random random = new Random();

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        int probability = random.nextInt(100) + 1; // Generate random number from 1 to 100

        if (probability <= 1) {
            event.reply(":partying_face: wow looks like you **win!** :tada:").setEphemeral(true).queue();
        } else if (probability <= 5) {
            event.reply("what's the score btw?").setEphemeral(true).queue();
        } else if (probability <= 20) {
            event.reply("what was that?").setEphemeral(true).queue();
        } else {
            event.reply("pong").setEphemeral(true).queue();
        }
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
