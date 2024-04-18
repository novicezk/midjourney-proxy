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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class GetErrorMessagesCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "get-log";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member != null && member.getRoles().stream().anyMatch(role -> role.getId().equals(Config.getGodfatherId()))) {
            String logs = listToString(ErrorMessageStorage.getErrorMessages());
            String stats = getEvents(EventsStorage.getStatistics());
            String description = "";

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Grand logs");

            if (logs.length() <= 1000) {
                builder.addField("Full logs: ", "```" + logs + "```", false);
            } else {
                FileUpload logsFile = getFileFromString(logs, "logs");
                if (logsFile != null) {
                    description = "logs sent to <#" + Config.getLogsChannel() + ">\n";
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        TextChannel logsChannel = guild.getTextChannelById(Config.getLogsChannel());
                        if (logsChannel != null) {
                            logsChannel.sendFiles(logsFile).queue();
                        }
                    }
                }
            }

            if (stats.length() <= 1000) {
                builder.addField("Stats: ", "```" + stats + "```", false);
            } else {
                FileUpload statsFile = getFileFromString(stats, "stats");
                if (statsFile != null) {
                    description = description + "stats sent to <#" + Config.getLogsChannel() + ">";
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        TextChannel logsChannel = guild.getTextChannelById(Config.getLogsChannel());
                        if (logsChannel != null) {
                            logsChannel.sendFiles(statsFile).queue();
                        }
                    }
                }
            }

            if (!description.isEmpty()) {
                builder.setDescription(description);
            }

            event.getHook().sendMessageEmbeds(builder.build())
                    .setEphemeral(true)
                    .queue();
        } else {
            // Getting the user id
            String userId = event.getUser().getId();
            // We get a list of errors by user id
            List<String> userErrorMessages = ErrorMessageStorage.getErrorMessages(userId);
            String logs = listToString(userErrorMessages);

            String description = "";
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("User logs");
            if (logs.length() <= 1000) {
                builder.addField("Collected for " + event.getUser().getGlobalName(), "```" + logs + "```", false);
            } else {
                FileUpload userLogs = getFileFromString(logs, "user_logs");
                if (userLogs != null) {
                    description = "The file have been sent to your private messages. Please check your DMs.";
                    User user = event.getUser();

                    user.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage("Hello! Here's your log file.")
                                .addFiles(userLogs)
                                .queue();
                    });
                }
            }

            if (!description.isEmpty()) {
                builder.setDescription(description);
            }

            // Convert the list of errors into a string and send it to the user
            event.getHook().sendMessageEmbeds(builder.build())
                    .setEphemeral(true)
                    .queue();
        }
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
        for (int i = 0; i < events.size(); i++) {
            EventData eventData = events.get(i);
            stringBuilder.append(
                    "id: " + eventData.getId() + ". " + eventData.getAction() + " user-id: " + eventData.getUserId() + " " + eventData.getTimestamp()
            ).append("\n");
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
