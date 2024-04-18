package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.ImageDownloader;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrivateMessageSender {
    public void sendToUser(ButtonInteractionEvent event) {
        Member member = event.getMember();
        if (member != null) {
            List<Message.Attachment> attachments = event.getMessage().getAttachments();
            List<FileUpload> files = new ArrayList<>();
            for (Message.Attachment attachment : attachments) {
                try {
                    File imageFile = ImageDownloader.downloadImage(attachment.getUrl());
                    files.add(FileUpload.fromData(imageFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            User user = member.getUser();
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Hi there!\n\n" +
                                "If you're looking for an avatar like the one in the picture just reach out to <@" + Config.getContactManagerId() + ">!")
                        .addFiles(files)
                        .queue();
            });
        }
    }

    public void sendToContactManager(MessageReceivedEvent event) {
        event.getJDA().retrieveUserById(Config.getContactManagerId()).queue(contactManager -> {
            if (contactManager != null) {
                contactManager.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Received message from <@" + event.getAuthor().getId() + ">\n\ncontent:\n"
                                    + event.getMessage().getContentRaw())
                            .queue();
                    event.getAuthor().openPrivateChannel().queue(channel -> {
                        channel.sendMessageEmbeds(EmbedUtil.createEmbed("Your message has been sent to the team!")).queue();
                    });
                });
            }
        });
    }
}
