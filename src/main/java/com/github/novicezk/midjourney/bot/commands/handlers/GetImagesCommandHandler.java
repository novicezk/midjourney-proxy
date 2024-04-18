package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class GetImagesCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "get-images";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        List<String> imageUrls = CommandsUtil.getUserUrls(event.getUser().getId());
        String title = CommandsUtil.generateTitle(imageUrls.isEmpty(), "Your uploaded images:\n");

        if (imageUrls.isEmpty() && CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser()) != null) {
            imageUrls.add(CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser()));
        }

        if (!imageUrls.isEmpty()) {
            event.getHook().sendMessage(title + formatImageUrls(imageUrls)).setEphemeral(true).queue();
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private String formatImageUrls(List<String> imageUrls) {
        StringBuilder validImageUrls = new StringBuilder();
        for (String url : imageUrls) {
            validImageUrls.append(url).append("\n");
        }
        return validImageUrls.toString();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
