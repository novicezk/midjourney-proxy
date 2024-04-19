package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.ErrorMessageStorage;
import com.github.novicezk.midjourney.bot.events.EventsStorage;
import com.github.novicezk.midjourney.bot.events.model.EventData;
import com.github.novicezk.midjourney.bot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class GetErrorMessagesCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "get-log";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member != null && member.getRoles().stream().anyMatch(role -> role.getId().equals(Config.getGodfatherId()))) {
            handleGodfatherEvent(event);
        } else {
            handleUserEvent(event);
        }
    }

    private void handleGodfatherEvent(SlashCommandInteractionEvent event) {
        String logs = listToString(ErrorMessageStorage.getErrorMessages());
        String stats = getEvents(EventsStorage.getStatistics());
        EmbedBuilder builder = new EmbedBuilder().setTitle("Grand logs");
        String description = sendGrandContentIfNeeded(event.getGuild(), logs, stats, builder);
        sendResponse(event, builder, description);
    }

    private void handleUserEvent(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        List<String> userErrorMessages = ErrorMessageStorage.getErrorMessages(userId);
        String logs = listToString(userErrorMessages);
        EmbedBuilder builder = new EmbedBuilder();
        String description = sendUserContentIfNeeded(event.getUser(), logs, builder);
        sendResponse(event, builder, description);
    }

    private String sendGrandContentIfNeeded(Guild guild, String logs, String stats, EmbedBuilder builder) {
        String description = "";
        if (logs.length() <= 1000) {
            builder.addField("Full logs: ", "```" + logs + "```", false);
        } else {
            FileUpload logsFile = getFileFromString(logs, "logs");
            description = sendFileIfNeeded(guild, logsFile);
        }

        if (stats != null && stats.length() <= 1000) {
            builder.addField("Stats: ", "```" + stats + "```", false);
        } else if (stats != null) {
            FileUpload statsFile = getFileFromString(stats, "stats");
            description = sendFileIfNeeded(guild, statsFile);
        }
        return description;
    }

    private String sendUserContentIfNeeded(User user, String logs, EmbedBuilder builder) {
        String description = "";
        if (logs.length() <= 1000) {
            builder.addField("User logs: ", "```" + logs + "```", false);
        } else {
            FileUpload logsFile = getFileFromString(logs, "user_logs");
            description = sendPrivateFileIfNeeded(user, logsFile);
        }

        return description;
    }

    private String sendPrivateFileIfNeeded(User user, FileUpload file) {
        if (file != null) {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Hello! Here's your log file.")
                        .addFiles(file)
                        .queue();
            });

            return "The file have been sent to your private messages. Please check your DMs.";
        }
        return "";
    }

    private String sendFileIfNeeded(Guild guild, FileUpload file) {
        if (file != null) {
            String description = "logs sent to <#" + Config.getLogsChannel() + ">";
            TextChannel logsChannel = guild.getTextChannelById(Config.getLogsChannel());
            if (logsChannel != null) {
                logsChannel.sendFiles(file).queue();
            }
            return description;
        }
        return "";
    }

    private void sendResponse(SlashCommandInteractionEvent event, EmbedBuilder builder, String description) {
        if (!description.isEmpty()) {
            builder.setDescription(description);
        }
        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }

    private static FileUpload getFileFromString(String content, String filename) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

        File file = new File(filename + "." + timeStamp + ".txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            return FileUpload.fromData(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getEvents(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            return "No records";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (EventData eventData : events) {
            stringBuilder.append(String.format(
                    "id: %-4d %-10s user-id: %-20s %s%n",
                    eventData.getId(),
                    eventData.getAction(),
                    eventData.getUserId(),
                    eventData.getTimestamp()
            ));
        }
        return stringBuilder.toString();
    }

    private static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "No records";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(i + 1).append(". ").append(list.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
