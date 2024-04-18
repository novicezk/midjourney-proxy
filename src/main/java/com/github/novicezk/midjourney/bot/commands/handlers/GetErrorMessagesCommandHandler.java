package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.ErrorMessageStorage;
import com.github.novicezk.midjourney.bot.utils.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class GetErrorMessagesCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "get-log";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member != null && member.getRoles().stream().anyMatch(role -> role.getId().equals(Config.getGodfatherId()))) {
            event.getHook().sendMessage("**Full logs:** \n\n" + listToString(ErrorMessageStorage.getErrorMessages()))
                    .setEphemeral(true)
                    .queue();
        } else {
            // Getting the user id
            String userId = event.getUser().getId();
            // We get a list of errors by user id
            List<String> userErrorMessages = ErrorMessageStorage.getErrorMessages(userId);
            // Convert the list of errors into a string and send it to the user
            event.getHook().sendMessage(listToString(userErrorMessages))
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static String listToString(List<String> list) {
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
