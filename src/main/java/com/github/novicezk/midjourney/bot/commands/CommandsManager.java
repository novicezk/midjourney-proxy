package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.commands.handlers.*;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.controller.SubmitController;
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
    private final GetErrorMessagesCommandHandler errorMessagesCommandHandler;
    private final UploadImageCommandHandler uploadImageCommandHandler;
    private final ContractCommandHandler contractCommandHandler;
    private final GenerateCommandHandler generateCommandHandler;
    private final GetImagesCommandHandler imagesCommandHandler;
    private final PingCommandHandler pingCommandHandler;

    public CommandsManager(SubmitController submitController) {
        generateCommandHandler = new GenerateCommandHandler(submitController);
        contractCommandHandler = new ContractCommandHandler(submitController);
        errorMessagesCommandHandler = new GetErrorMessagesCommandHandler();
        uploadImageCommandHandler = new UploadImageCommandHandler();
        imagesCommandHandler = new GetImagesCommandHandler();
        pingCommandHandler = new PingCommandHandler();
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
        commandData.add(Commands.slash("generate", "Need some inspiration? Use this command to generate random images!"));
        commandData.add(Commands.slash("get-log", "Logs file"));
        commandData.add(Commands.slash("ping", "default ping command(?)"));

        event.getGuild().updateCommands().addCommands(commandData).queue();

        // clear queue on start
        QueueManager.clearQueue(event.getGuild());
    }
}
