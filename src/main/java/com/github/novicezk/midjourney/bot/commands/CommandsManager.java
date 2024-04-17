package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.bot.error.ErrorMessageStorage;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.images.ImageBBUploader;
import com.github.novicezk.midjourney.bot.images.ImageStorage;
import com.github.novicezk.midjourney.bot.images.ImageValidator;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.model.images.ImageResponse;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.entities.Message.Attachment;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsManager extends ListenerAdapter {
    private final SubmitController submitController;

    public CommandsManager(SubmitController submitController) {
        this.submitController = submitController;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "upload-image":
                handleUploadImageCommand(event);
                break;
            case "get-images":
                handleGetImagesCommand(event);
                break;
            case "generate":
                handleGenerateCommand(event);
                break;
            case "get-log":
                handleGetErrorMessagesCommand(event);
                break;
            case "ping":
                handlePingCommand(event);
                break;
            default:
                break;
        }
    }

    private void handlePingCommand(SlashCommandInteractionEvent event) {
        event.reply("what was that?").queue();
    }

    private void handleGetErrorMessagesCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member != null && member.getRoles().stream().anyMatch(role -> role.getId().equals(Config.getGodfatherId()))) {
            event.reply("**Full logs:** \n\n" + listToString(ErrorMessageStorage.getErrorMessages()))
                    .setEphemeral(true)
                    .queue();
        } else {
            // Getting the user id
            String userId = event.getUser().getId();
            // We get a list of errors by user id
            List<String> userErrorMessages = ErrorMessageStorage.getErrorMessages(userId);
            // Convert the list of errors into a string and send it to the user
            event.reply(listToString(userErrorMessages))
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static String listToString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(i + 1).append(". ").append(list.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }

    private void handleUploadImageCommand(SlashCommandInteractionEvent event) {
        // Defer reply to avoid timeout
        event.deferReply().setEphemeral(true).queue();

        OptionMapping mainImageOption = event.getOption("main_image");
        if (mainImageOption != null && mainImageOption.getAsAttachment().isImage()) {
            List<String> imageUrls = extractImageUrls(event);
            if (!imageUrls.isEmpty()) {
                ImageStorage.addImageUrl(event.getUser().getId(), imageUrls);
                event.getHook().sendMessage("Your images are in! Now you can use `/generate` to start generating characters or try `/get-images` to see what you've uploaded.")
                        .setEphemeral(true)
                        .queue();
            } else {
                OnErrorAction.onImageValidateErrorMessage(event);
            }
        } else {
            OnErrorAction.onImageValidateErrorMessage(event);
        }
    }

    private List<String> extractImageUrls(SlashCommandInteractionEvent event) {
        List<String> imageUrls = new ArrayList<>();
        OptionMapping mainImageOption = event.getOption("main_image");
        Attachment mainImage = mainImageOption.getAsAttachment();

        ImageResponse uploadedImageResponse = ImageBBUploader.uploadImageNew(mainImage.getUrl());
        if (uploadedImageResponse != null && uploadedImageResponse.getData().getUrl() != null) {
            imageUrls.add(uploadedImageResponse.getData().getUrl());
        }

        for (int i = 2; i <= 4; i++) {
            OptionMapping imageOption = event.getOption("image" + i);
            if (imageOption != null && imageOption.getAsAttachment().isImage()) {
                Attachment attachment = imageOption.getAsAttachment();
                ImageResponse response = ImageBBUploader.uploadImageNew(attachment.getUrl());
                if (response != null && response.getData().getUrl() != null) {
                    imageUrls.add(response.getData().getUrl());
                }
            }
        }
        return imageUrls;
    }

    private void handleGetImagesCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        List<String> imageUrls = getUserUrls(event.getUser().getId());
        String title = generateTitle(imageUrls.isEmpty(), "Your uploaded images:\n");

        if (imageUrls.isEmpty() && getImageUrlFromDiscordAvatar(event.getUser()) != null) {
            imageUrls.add(getImageUrlFromDiscordAvatar(event.getUser()));
        }

        if (!imageUrls.isEmpty()) {
            event.getHook().sendMessage(title + formatImageUrls(imageUrls)).setEphemeral(true).queue();
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private void handleGenerateCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        List<String> imageUrls = getUserUrls(event.getUser().getId());
        String title = generateTitle(imageUrls.isEmpty(), "");

        if (imageUrls.isEmpty()) {
            String discordAvatarUrl = getImageUrlFromDiscordAvatar(event.getUser());
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
            log.debug("ADD to queue - {}", result.getResult());
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

    private String generateTitle(boolean isImagesEmpty, String defaultTitle) {
        if (isImagesEmpty) {
            return "Oops! No image uploaded or link expired. We'll use your avatar instead. To upload a new image, try `/upload-image`.\n\n";
        } else {
            return defaultTitle;
        }
    }

    private String formatImageUrls(List<String> imageUrls) {
        StringBuilder validImageUrls = new StringBuilder();
        for (String url : imageUrls) {
            validImageUrls.append(url).append("\n");
        }
        return validImageUrls.toString();
    }

    private List<String> getUserUrls(String userId) {
        List<String> imageUrls = new ArrayList<>();
        for (String url : ImageStorage.getImageUrls(userId)) {
            if (ImageValidator.isValidImageUrl(url)) {
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    private String getImageUrlFromDiscordAvatar(User user) {
        String url = null;

        if (user.getAvatarUrl() != null) {
            url = user.getAvatarUrl().replace(".gif", ".png");
        }

        return url;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "main_image", "Choose your image", true);
        OptionData attachment2 = new OptionData(OptionType.ATTACHMENT, "image2", "Optional image", false);
        OptionData attachment3 = new OptionData(OptionType.ATTACHMENT, "image3", "Optional image", false);
        OptionData attachment4 = new OptionData(OptionType.ATTACHMENT, "image4", "Optional image", false);
        commandData.add(Commands.slash("upload-image", "Upload your image to generate something amazing!")
                .addOptions(attachment, attachment2, attachment3, attachment4));
        commandData.add(Commands.slash("get-images", "Get your currently uploaded images."));
        commandData.add(Commands.slash("generate", "Need some inspiration? Use this command to generate random images!"));
        commandData.add(Commands.slash("get-log", "Logs file"));
        commandData.add(Commands.slash("ping", "default fockin ping"));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
