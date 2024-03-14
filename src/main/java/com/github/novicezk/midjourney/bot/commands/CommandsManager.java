package com.github.novicezk.midjourney.bot.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {

            case "upload-image":
                event.reply("got it!").setEphemeral(true).queue();
                break;

            default:
                break;
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "image", "choose your image", true);
        commandData.add(Commands.slash("upload-image", "Uploading your image for generating").addOptions(attachment));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
