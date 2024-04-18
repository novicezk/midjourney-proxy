package com.github.novicezk.midjourney.bot.error;

import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class OnErrorAction {

    public static void onImageErrorMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! We couldn't find any image. Please run the command `/upload-image` and try again.", false);
    }

    public static void onImageValidateErrorMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! Something went wrong. Please double check and make sure you've selected an image file.", true);
    }

    public static void onMissingRoleMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! You're missing the required role.", false);
    }

    public static void onMissingFieldMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Oops! You're missing the required field.", false);
    }

    public static void onQueueFullMessage(SlashCommandInteractionEvent event) {
        sendMessage(event, "Looks like you've reached the queue limit. Please wait while we work on your current requests!", false);
    }

    private static void sendMessage(GenericCommandInteractionEvent event, String message, boolean error) {
        if (error) {
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbedError(message))).queue();
        } else {
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed(message))).queue();
        }
    }
}
