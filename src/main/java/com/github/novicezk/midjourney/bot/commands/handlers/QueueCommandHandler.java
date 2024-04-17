package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.queue.QueueEntry;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Comparator;
import java.util.List;

public class QueueCommandHandler {

    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (event.getName().equals("clear-queue")) {
            handleClear(event);
        } else {
            handleGet(event);
        }
    }

    private void handleClear(SlashCommandInteractionEvent event) {
        QueueManager.clearQueue(event.getGuild());
        event.getHook().sendMessage("Queue has been cleared!").queue();
    }

    private void handleGet(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(listToString(QueueManager.getCurrentQueue())).queue();
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
}
