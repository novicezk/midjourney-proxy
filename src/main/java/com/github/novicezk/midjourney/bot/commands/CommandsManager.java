package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.bot.commands.handlers.*;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.bot.utils.WelcomeMessageTracker;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsManager extends ListenerAdapter {
    private final List<CommandHandler> commandHandlers;
    private final PrivateMessageSender privateMessageSender;
    private final SubmitController submitController;

    public CommandsManager(SubmitController submitController) {
        this.commandHandlers = initializeCommandHandlers(submitController);
        this.privateMessageSender = new PrivateMessageSender();
        this.submitController = submitController;
    }

    private List<CommandHandler> initializeCommandHandlers(SubmitController submitController) {
        List<CommandHandler> handlers = new ArrayList<>();
        handlers.add(new GetErrorMessagesCommandHandler());
        handlers.add(new UploadImageCommandHandler());
        handlers.add(new ContractCommandHandler(submitController));
        handlers.add(new GenerateCommandHandler(submitController));
        handlers.add(new GetImagesCommandHandler());
        handlers.add(new PingCommandHandler());
        handlers.add(new QueueCommandHandler());
        return handlers;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannelType().equals(ChannelType.PRIVATE) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            privateMessageSender.sendToContactManager(event);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        EventsManager.onCommand(event);
        for (CommandHandler handler : commandHandlers) {
            if (handler.supports(event.getName())) {
                handler.handle(event);
                return;
            }
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
        commandData.add(Commands.slash(UploadImageCommandHandler.COMMAND_NAME, "Upload your image to generate something amazing!")
                .addOptions(attachment, attachment2, attachment3, attachment4));

        // contract command
        OptionData promptContract = new OptionData(OptionType.STRING, "prompt", "Prompt to use the contract command");
        OptionData idContract = new OptionData(OptionType.STRING, "task", "Task id");
        commandData.add(Commands.slash(ContractCommandHandler.COMMAND_NAME, "admins only").addOptions(promptContract, idContract));

        // other commands
        commandData.add(Commands.slash(GetImagesCommandHandler.COMMAND_NAME, "Get your currently uploaded images."));
        commandData.add(Commands.slash(GenerateCommandHandler.COMMAND_NAME, "Need some inspiration? Use this command to generate images!"));
        commandData.add(Commands.slash(GetErrorMessagesCommandHandler.COMMAND_NAME, "Logs file"));
        commandData.add(Commands.slash(PingCommandHandler.COMMAND_NAME, "default ping command(or?)"));
        commandData.add(Commands.slash(QueueCommandHandler.COMMAND_NAME_GET, "Check the current queue status."));
        commandData.add(Commands.slash(QueueCommandHandler.COMMAND_NAME_CLEAR, "admins only"));

        event.getGuild().updateCommands().addCommands(commandData).queue();

        // clear queue on start
        QueueManager.clearQueue(event.getGuild());
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        EventsManager.onButtonClick(event);

        event.deferReply().setEphemeral(true).queue();
        String buttonUserId = event.getUser().getId();

        if (event.getComponentId().equals("create")) {
            privateMessageSender.sendToUser(event);
            event.getHook().sendMessageEmbeds(
                    EmbedUtil.createEmbed("We've sent you a private message please check your DMs.")
            ).queue();
            return;
        }

        if (!event.getMessage().getContentRaw().contains(buttonUserId)) {
            event.getHook().sendMessageEmbeds(
                    EmbedUtil.createEmbedWarning("Only the original author can delete the request.")
            ).queue();
            return;
        }

        if (event.getComponentId().equals("delete")) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
            event.getHook().sendMessageEmbeds(
                    EmbedUtil.createEmbed("The post has been deleted.")
            ).queue();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();

        log.debug("onGuildMemberJoin");
        if (user.getAvatarUrl() != null && !WelcomeMessageTracker.hasBeenWelcomed(user.getId())) {
            log.debug("onGuildMemberJoin hasBeenWelcomed");

            String discordAvatarUrl = CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser());
            GeneratedPromptData promptData = new PromptGenerator().generatePrompt(discordAvatarUrl, event.getUser());

            SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
            imagineDTO.setPrompt(promptData.getPrompt());
            processPromptData(promptData, event);
        }
    }

    private void processPromptData(GeneratedPromptData promptData, GuildMemberJoinEvent event) {
        String postText = promptData.getMessage();
        SeasonTracker.incrementGenerationCount();
        WelcomeMessageTracker.markAsWelcomed(event.getUser().getId());

        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(promptData.getPrompt());
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            handleCommandResponse(result, postText, promptData.getPrompt(), event);
        }
    }

    private void handleCommandResponse(
            SubmitResultVO result,
            String postText,
            String prompt,
            GuildMemberJoinEvent event
    ) {
        if (result.getCode() == ReturnCode.SUCCESS || result.getCode() == ReturnCode.IN_QUEUE) {
            QueueManager.addToQueue(event.getGuild(), prompt, event.getUser().getId(), result.getResult(), postText);
        } else {
            ErrorMessageHandler.sendMessage(
                    event.getGuild(),
                    event.getUser().getId(),
                    "Critical miss! \uD83C\uDFB2\uD83E\uDD26 \nTry again or upload new image!",
                    result.getCode() + " " + result.getDescription()
            );
            log.error("{}: {}", result.getCode(), result.getDescription());
        }
    }
}
