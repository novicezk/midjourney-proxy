package com.github.novicezk.midjourney.bot.error;

import com.github.novicezk.midjourney.bot.utils.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

public class ErrorMessageHandler {
    public static void sendMessage(@Nullable Guild guild, String userId, String text, String failReason) {
        if (guild != null) {
            TextChannel channel = guild.getTextChannelById(Config.getSendingChannel());
            if (channel != null) {
                channel.sendMessage("<@" + userId + "> \n\n" + text).queue();

                // Save the reason
                ErrorMessageStorage.saveErrorMessage(userId, failReason);
            }
        }
    }
}
