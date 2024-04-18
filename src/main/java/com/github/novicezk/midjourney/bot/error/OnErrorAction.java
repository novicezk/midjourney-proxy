package com.github.novicezk.midjourney.bot.error;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class OnErrorAction {

    public static void onImageErrorMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! We couldn't find any image. Please run the command `/upload-image` and try again.");
    }

    public static void onImageValidateErrorMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! Something went wrong. Please double check and make sure you've selected an image file.");
    }

    public static void onMissingRoleMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! You're missing the required role.");
    }

    public static void onMissingFieldMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! You're missing the required field.");
    }

    public static void onQueueFullMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "You're already in the queue so just sit back and relax \uD83D\uDE0E");
    }

    private static void sendMessage(GenericCommandInteractionEvent event, String message) {
        event.getHook().sendMessage(message).queue();
    }
}
