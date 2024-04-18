package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

@Slf4j
public class GenerateCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "generate";

    private final SubmitController submitController;

    public GenerateCommandHandler(final SubmitController submitController) {
        this.submitController = submitController;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        List<String> imageUrls = CommandsUtil.getUserUrls(event.getUser().getId());
        String title = CommandsUtil.generateTitle(imageUrls.isEmpty(), "");

        if (imageUrls.isEmpty()) {
            String discordAvatarUrl = CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser());
            if (discordAvatarUrl != null) {
                imageUrls.add(discordAvatarUrl);
            }
        }

        if (imageUrls.isEmpty()) {
            OnErrorAction.onImageErrorMessage(event);
            return;
        }

        if (QueueManager.reachLimitQueue(event.getUser().getId())) {
            OnErrorAction.onQueueFullMessage(event);
            return;
        }

        GeneratedPromptData promptData = new PromptGenerator().generatePrompt(imageUrls, event.getUser());
        processPromptData(promptData, title, event);
    }

    private void processPromptData(GeneratedPromptData promptData, String title, SlashCommandInteractionEvent event) {
        String postText = title + promptData.getMessage();
        SeasonTracker.incrementGenerationCount();

        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(promptData.getPrompt());
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            CommandsUtil.handleCommandResponse(result, postText, promptData.getPrompt(), event);
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
