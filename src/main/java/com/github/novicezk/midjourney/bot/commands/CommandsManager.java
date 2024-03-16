package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.images.ImageStorage;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
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
        OptionMapping mainImageOption = event.getOption("main_image");

        if (mainImageOption != null && mainImageOption.getAsAttachment().isImage()) {
            List<String> imageUrls = extractImageUrls(event);
            ImageStorage.addImageUrl(event.getUser().getId(), imageUrls);
            event.reply("Your images have been successfully uploaded! Now you can use the command `/generate` to get inspired.")
                    .setEphemeral(true)
                    .queue();
        } else {
            OnErrorAction.imageValidateErrorMessage(event);
        }
    }

    private List<String> extractImageUrls(SlashCommandInteractionEvent event) {
        List<String> imageUrls = new ArrayList<>();
        OptionMapping mainImageOption = event.getOption("main_image");
        Message.Attachment mainImage = mainImageOption.getAsAttachment();
        imageUrls.add(mainImage.getUrl());

        for (int i = 2; i <= 4; i++) {
            OptionMapping imageOption = event.getOption("image" + i);
            if (imageOption != null && imageOption.getAsAttachment().isImage()) {
                Message.Attachment attachment = imageOption.getAsAttachment();
                imageUrls.add(attachment.getUrl());
            }
        }
        return imageUrls;
    }

    private void handleGetImagesCommand(SlashCommandInteractionEvent event) {
        List<String> imageUrls = ImageStorage.getImageUrls(event.getUser().getId());
        if (imageUrls != null && !imageUrls.isEmpty()) {
            StringBuilder response = new StringBuilder("Your uploaded images:\n");
            for (String url : imageUrls) {
                response.append(url).append("\n");
            }
            event.reply(response.toString()).setEphemeral(true).queue();
        } else {
            OnErrorAction.imageErrorMessage(event);
        }
    }

    private void handleGenerateCommand(SlashCommandInteractionEvent event) {
        List<String> imageUrls = ImageStorage.getImageUrls(event.getUser().getId());
        if (imageUrls != null && !imageUrls.isEmpty()) {
            PromptGenerator promptGenerator = new PromptGenerator();
            event.reply(promptGenerator.generatePrompt(imageUrls).getPrompt()).setEphemeral(true).queue();
        } else {
            OnErrorAction.imageErrorMessage(event);
        }
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
