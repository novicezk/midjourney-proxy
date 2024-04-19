package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;

public class EmbedCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "create-embed";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        OptionMapping descriptionMapping = event.getOption("description");
        OptionMapping channelMapping = event.getOption("channel");
        OptionMapping titleMapping = event.getOption("title");
        OptionMapping footerMapping = event.getOption("footer");
        OptionMapping colorMapping = event.getOption("color");

        Guild guild = event.getGuild();

        if (descriptionMapping == null || channelMapping == null || guild == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        String description = descriptionMapping.getAsString();
        GuildChannelUnion channelUnion = channelMapping.getAsChannel();
        TextChannel channel = guild.getTextChannelById(channelUnion.getId());

        if (channel == null) {
            OnErrorAction.sendMessage(event, "Channel is not found", true);
            return;
        }

        event.getHook().sendMessage("Embed has been sent.").setEphemeral(true).queue();

        Color color = Color.white;
        try {
            if (colorMapping != null) {
                color = Color.decode(colorMapping.getAsString());
            }
        } catch (NumberFormatException ex) {
            OnErrorAction.sendMessage(event, ex.getMessage(), true);
        }

        channel.sendMessageEmbeds(EmbedUtil.createEmbed(
                titleMapping != null ? titleMapping.getAsString() : null,
                description,
                footerMapping != null ? footerMapping.getAsString() : null,
                color
        )).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
