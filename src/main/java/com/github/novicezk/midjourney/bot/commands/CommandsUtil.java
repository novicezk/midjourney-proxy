package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.bot.images.ImageStorage;
import com.github.novicezk.midjourney.bot.images.ImageValidator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsUtil {
    public static List<String> getUserUrls(String userId) {
        List<String> imageUrls = new ArrayList<>();
        for (String url : ImageStorage.getImageUrls(userId)) {
            if (ImageValidator.isValidImageUrl(url)) {
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    public static String generateTitle(boolean isImagesEmpty, String defaultTitle) {
        if (isImagesEmpty) {
            return "Oops! No image uploaded or link expired; we'll use your avatar instead. \nTo upload a new image try `/upload-image`.\n\n";
        } else {
            return defaultTitle;
        }
    }

    public static String getImageUrlFromDiscordAvatar(User user) {
        String url = null;

        if (user.getAvatarUrl() != null) {
            url = user.getAvatarUrl().replace(".gif", ".png");
        }

        return url;
    }

    public static void handleCommandResponse(
            SubmitResultVO result,
            String postText,
            String prompt,
            SlashCommandInteractionEvent event
    ) {
        if (result.getCode() == ReturnCode.SUCCESS || result.getCode() == ReturnCode.IN_QUEUE) {
            QueueManager.addToQueue(event.getGuild(), prompt, event.getUser().getId(), result.getResult(), postText);
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("You're in the queue! \uD83E\uDD73"))).queue();
        } else {
            ErrorMessageHandler.sendMessage(
                    event.getGuild(),
                    event.getUser().getId(),
                    "Critical miss! \uD83C\uDFB2\uD83E\uDD26 \nTry again or upload new image!",
                    result.getCode() + " " + result.getDescription()
            );
            event.getHook().deleteOriginal().queue();
            log.error("{}: {}", result.getCode(), result.getDescription());
        }
    }
}
