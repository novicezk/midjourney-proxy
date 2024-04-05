package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.images.ImageBBUploader;
import com.github.novicezk.midjourney.bot.images.ImageStorage;
import com.github.novicezk.midjourney.bot.images.ImageValidator;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.model.images.ImageResponse;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsManager extends ListenerAdapter {

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
            default:
                break;
        }
    }

    private void handleUploadImageCommand(SlashCommandInteractionEvent event) {
        // Defer reply to avoid timeout
        event.deferReply().queue();

        OptionMapping mainImageOption = event.getOption("main_image");
        if (mainImageOption != null && mainImageOption.getAsAttachment().isImage()) {
            List<String> imageUrls = extractImageUrls(event);
            if (!imageUrls.isEmpty()) {
                ImageStorage.addImageUrl(event.getUser().getId(), imageUrls);
                event.getHook().sendMessage("Your images have been successfully uploaded! Now you can use the command `/generate` to get inspired.")
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
        Message.Attachment mainImage = mainImageOption.getAsAttachment();

        ImageResponse uploadedImageResponse = ImageBBUploader.uploadImageNew(mainImage.getUrl());
        if (uploadedImageResponse != null && uploadedImageResponse.getData().getUrl() != null) {
            imageUrls.add(uploadedImageResponse.getData().getUrl());
        }

        for (int i = 2; i <= 4; i++) {
            OptionMapping imageOption = event.getOption("image" + i);
            if (imageOption != null && imageOption.getAsAttachment().isImage()) {
                Message.Attachment attachment = imageOption.getAsAttachment();
                ImageResponse response = ImageBBUploader.uploadImageNew(attachment.getUrl());
                if (response != null && response.getData().getUrl() != null) {
                    imageUrls.add(response.getData().getUrl());
                }
            }
        }
        return imageUrls;
    }

    private void handleGetImagesCommand(SlashCommandInteractionEvent event) {
        List<String> imageUrls = getUserUrls(event.getUser().getId());
        String title = generateTitle(imageUrls.isEmpty(), "Your uploaded images:\n");

        if (imageUrls.isEmpty() && getImageUrlFromDiscordAvatar(event.getUser()) != null) {
            imageUrls.add(getImageUrlFromDiscordAvatar(event.getUser()));
        }

        if (!imageUrls.isEmpty()) {
            event.reply(title + formatImageUrls(imageUrls)).setEphemeral(true).queue();
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private void handleGenerateCommand(SlashCommandInteractionEvent event) {
        List<String> imageUrls = getUserUrls(event.getUser().getId());
        String title = generateTitle(imageUrls.isEmpty(), "");

        if (imageUrls.isEmpty() && getImageUrlFromDiscordAvatar(event.getUser()) != null) {
            imageUrls.add(getImageUrlFromDiscordAvatar(event.getUser()));
        }

        if (!imageUrls.isEmpty()) {
            GeneratedPromptData promptData =
                    new PromptGenerator().generatePrompt(imageUrls, event.getUser());

            event.reply(title + promptData.getMessage()).setEphemeral(true).queue();
        } else {
            OnErrorAction.onImageErrorMessage(event);
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

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
