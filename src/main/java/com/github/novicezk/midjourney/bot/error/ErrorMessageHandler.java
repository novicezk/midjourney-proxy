package com.github.novicezk.midjourney.bot.error;

import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ErrorMessageHandler {
    public static void sendMessage(@Nullable Guild guild, String userId, String text, String failReason) {
        EventsManager.onErrorEvent(userId, failReason);
        if (guild != null) {
            TextChannel channel = guild.getTextChannelById(Config.getSendingChannel());
            if (channel != null) {
                channel.sendMessageEmbeds(List.of(EmbedUtil.createEmbedError("<@" + userId + "> \n\n" + text))).queue();

                // Save the reason
                ErrorMessageStorage.saveErrorMessage(userId, failReason);
            }
        }
    }
}
