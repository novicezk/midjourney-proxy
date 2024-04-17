package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
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
public class GenerateCommandHandler {
    private final SubmitController submitController;

    public GenerateCommandHandler(final SubmitController submitController) {
        this.submitController = submitController;
    }

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

        // TODO add the queue limits
//        if (QueueManager.isUserInQueue(event.getUser().getId())) {
//            OnErrorAction.queueMessage(event);
//            return;
//        }

        GeneratedPromptData promptData = new PromptGenerator().generatePrompt(imageUrls, event.getUser());
        processPromptData(promptData, title, event);
    }

    private void processPromptData(GeneratedPromptData promptData, String title, SlashCommandInteractionEvent event) {
        String text = title + promptData.getMessage();
        SeasonTracker.incrementGenerationCount();

        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(promptData.getPrompt());
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            handleCommandResponse(result, text, promptData.getPrompt(), event);
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private void handleCommandResponse(
            SubmitResultVO result,
            String text,
            String prompt,
            SlashCommandInteractionEvent event
    ) {
        if (result.getCode() == ReturnCode.SUCCESS || result.getCode() == ReturnCode.IN_QUEUE) {
            QueueManager.addToQueue(event.getGuild(), prompt, event.getUser().getId(), result.getResult(), text);
            event.getHook().sendMessage("You're in the queue! \uD83E\uDD73").queue();
        } else {
            ErrorMessageHandler.sendMessage(
                    event.getGuild(),
                    event.getUser().getId(),
                    "Critical miss! \uD83C\uDFB2\uD83E\uDD26 \nTry again or upload new image!",
                    result.getCode() + " " + result.getDescription()
            );
            event.getHook().deleteOriginal().queue();
            log.error("{}: {}", result.getCode(), result.getDescription());
        }
    }
}
