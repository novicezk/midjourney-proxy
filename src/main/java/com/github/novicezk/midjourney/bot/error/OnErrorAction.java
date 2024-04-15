package com.github.novicezk.midjourney.bot.error;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class OnErrorAction {

    public static void onImageErrorMessage(SlashCommandInteractionEvent event) {
        event.getHook()
                .sendMessage("Oops! We couldn't find any image. Please run the command `/upload-image` and try again.")
                .queue();
    }

    public static void onImageValidateErrorMessage(SlashCommandInteractionEvent event) {
        event.getHook()
                .sendMessage("Oops! Something went wrong. Please double check and make sure you've selected an image file.")
                .queue();
    }

    public static void queueMessage(SlashCommandInteractionEvent event) {
        event.getHook()
                .sendMessage("You're already in the queue so just sit back and relax \uD83D\uDE0E")
                .queue();
    }
}
