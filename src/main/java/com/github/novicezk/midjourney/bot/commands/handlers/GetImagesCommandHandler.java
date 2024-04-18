package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;

import java.util.List;

public class GetImagesCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "get-images";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        List<String> imageUrls = CommandsUtil.getUserUrls(event.getUser().getId());
        boolean noImageResponse = imageUrls.isEmpty();

        if (imageUrls.isEmpty() && CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser()) != null) {
            imageUrls.add(CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser()));
        }

        if (!imageUrls.isEmpty()) {
            WebhookMessageCreateAction<Message> action = event.getHook().sendMessage(formatImageUrls(imageUrls)).setEphemeral(true);
            if (noImageResponse) {
                action.addEmbeds(EmbedUtil.createEmbed(
                        "Oops! No image uploaded or link expired; we'll use your avatar instead. \nTo upload a new image try `/upload-image`."
                ));
            }
            action.queue();
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
