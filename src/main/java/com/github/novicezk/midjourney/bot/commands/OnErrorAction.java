package com.github.novicezk.midjourney.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class OnErrorAction {

    public static void onImageErrorMessage(SlashCommandInteractionEvent event) {
        event.reply("Oops! We couldn't find any image. Please run the command `/upload-image` and try again.")
                .setEphemeral(true)
                .queue();
    }

    public static void onImageValidateErrorMessage(SlashCommandInteractionEvent event) {
        event.reply("Oops! Something went wrong. Please double check and make sure you've selected an image file.")
                .setEphemeral(true)
                .queue();
    }

    public static void defaultMessage(SlashCommandInteractionEvent event) {
        event.reply("Sorry, but I couldn't complete your request at this time. Please try again later.")
                .setEphemeral(true)
                .queue();
    }
}
