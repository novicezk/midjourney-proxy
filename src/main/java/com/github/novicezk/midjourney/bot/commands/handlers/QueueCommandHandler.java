package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.queue.QueueEntry;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Comparator;
import java.util.List;

public class QueueCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME_GET = "get-queue";
    public static final String COMMAND_NAME_CLEAR = "clear-queue";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (event.getName().equals("clear-queue")) {
            handleClear(event);
        } else {
            handleGet(event);
        }
    }

    private void handleClear(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member != null && member.getRoles().stream().anyMatch(role ->
                role.getId().equals(Config.getAdminsRoleId()) || role.getId().equals(Config.getGodfatherId()))) {
            QueueManager.clearQueue(event.getGuild());
            event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("Queue has been cleared!")).queue();
        } else {
            OnErrorAction.onMissingRoleMessage(event);
        }
    }

    private void handleGet(SlashCommandInteractionEvent event) {
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(listToString(QueueManager.getCurrentQueue()))).queue();
    }

    private String listToString(List<QueueEntry> list) {
        StringBuilder sb = new StringBuilder();
        if (list.isEmpty()) {
            sb.append("Queue is empty!");
        }

        list.sort(Comparator.comparingInt(QueueEntry::getQueueIndex));
        for (int i = 0; i < list.size(); i++) {
            QueueEntry entry = list.get(i);
            sb.append(i).append(". <@").append(entry.getUserId()).append(">").append("\n");
        }

        return sb.toString();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME_GET.equals(eventName) || COMMAND_NAME_CLEAR.equals(eventName);
    }
}
