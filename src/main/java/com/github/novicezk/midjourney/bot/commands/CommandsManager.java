package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.commands.handlers.*;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.ImageDownloader;
import com.github.novicezk.midjourney.controller.SubmitController;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsManager extends ListenerAdapter {
    private final GetErrorMessagesCommandHandler errorMessagesCommandHandler;
    private final UploadImageCommandHandler uploadImageCommandHandler;
    private final ContractCommandHandler contractCommandHandler;
    private final GenerateCommandHandler generateCommandHandler;
    private final GetImagesCommandHandler imagesCommandHandler;
    private final PingCommandHandler pingCommandHandler;
    private final QueueCommandHandler queueCommandHandler;

    public CommandsManager(SubmitController submitController) {
        generateCommandHandler = new GenerateCommandHandler(submitController);
        contractCommandHandler = new ContractCommandHandler(submitController);
        errorMessagesCommandHandler = new GetErrorMessagesCommandHandler();
        uploadImageCommandHandler = new UploadImageCommandHandler();
        imagesCommandHandler = new GetImagesCommandHandler();
        queueCommandHandler = new QueueCommandHandler();
        pingCommandHandler = new PingCommandHandler();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User selfUser = event.getJDA().getSelfUser();
        if (event.getChannelType().equals(ChannelType.PRIVATE) && !selfUser.getId().equals(event.getAuthor().getId())) {
            User author = event.getAuthor();
            event.getJDA().retrieveUserById(Config.getContactManagerId()).queue(contactManager -> {
                if (contactManager != null) {
                    contactManager.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage("Recieved message from <@" + author.getId() + ">\n\ncontent:\n"
                                        + event.getMessage().getContentRaw())
                                .queue();

                        event.getAuthor().openPrivateChannel().queue(channel -> {
                            channel.sendMessage("Your message has been sent to the team!").queue();
                        });
                    });
                }
            });
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "get-log":
                errorMessagesCommandHandler.handle(event);
                break;
            case "upload-image":
                uploadImageCommandHandler.handle(event);
                break;
            case "generate":
                generateCommandHandler.handle(event);
                break;
            case "get-images":
                imagesCommandHandler.handle(event);
                break;
            case "contract":
                contractCommandHandler.handle(event);
                break;
            case "get-queue":
            case "clear-queue":
                queueCommandHandler.handle(event);
                break;

            case "ping":
            default:
                pingCommandHandler.handle(event);
                break;
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        // upload-image command
        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "main_image", "Choose your image", true);
        OptionData attachment2 = new OptionData(OptionType.ATTACHMENT, "image2", "Optional image", false);
        OptionData attachment3 = new OptionData(OptionType.ATTACHMENT, "image3", "Optional image", false);
        OptionData attachment4 = new OptionData(OptionType.ATTACHMENT, "image4", "Optional image", false);
        commandData.add(Commands.slash("upload-image", "Upload your image to generate something amazing!")
                .addOptions(attachment, attachment2, attachment3, attachment4));

        // contract command
        OptionData promptContract = new OptionData(OptionType.STRING, "prompt", "Prompt to use the contract command", true);
        commandData.add(Commands.slash("contract", "admins only").addOptions(promptContract));

        // other commands
        commandData.add(Commands.slash("get-images", "Get your currently uploaded images."));
        commandData.add(Commands.slash("generate", "Need some inspiration? Use this command to generate images!"));
        commandData.add(Commands.slash("get-log", "Logs file"));
        commandData.add(Commands.slash("ping", "default ping command(or?)"));
        commandData.add(Commands.slash("get-queue", "Check the current queue status."));
        commandData.add(Commands.slash("clear-queue", "admins only"));

        event.getGuild().updateCommands().addCommands(commandData).queue();

        // clear queue on start
        QueueManager.clearQueue(event.getGuild());
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String buttonUserId = event.getUser().getId();

        if (event.getComponentId().equals("create")) {
            sendPrivateMessage(event);
            event.getHook().sendMessage("We've sent you a private message please check your DMs.").queue();
            return;
        }

        if (!event.getMessage().getContentRaw().contains(buttonUserId)) {
            event.getHook().sendMessage("Only the original author can delete the request.").queue();
            return;
        }

        if (event.getComponentId().equals("delete")) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
            event.getHook().sendMessage("The post has been deleted.").queue();
        }
    }

    public void sendPrivateMessage(ButtonInteractionEvent event) {
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
}
