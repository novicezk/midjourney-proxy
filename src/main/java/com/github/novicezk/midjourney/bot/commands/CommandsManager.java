package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.images.ImageStorage;
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
                OptionMapping mainImageOption = event.getOption("main_image");

                if (mainImageOption != null && mainImageOption.getAsAttachment().isImage()) {
                    List<String> imageUrls = new ArrayList<>();

                    Message.Attachment mainImage = mainImageOption.getAsAttachment();
                    imageUrls.add(mainImage.getUrl());

                    for (int i = 2; i <= 4; i++) {
                        OptionMapping imageOption = event.getOption("image" + i);
                        if (imageOption != null && imageOption.getAsAttachment().isImage()) {
                            Message.Attachment attachment = imageOption.getAsAttachment();
                            imageUrls.add(attachment.getUrl());
                        }
                    }

                    ImageStorage.addImageUrl(event.getUser().getId(), imageUrls);
                    event.reply("Your images have been successfully uploaded.").setEphemeral(true).queue();
                } else {
                    OnErrorAction.imageValidateErrorMessage(event);
                }

                break;

            case "get-images":
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
                break;

            default:
                break;
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "main_image", "choose your image", true);
        OptionData attachment2 = new OptionData(OptionType.ATTACHMENT, "image2", "choose your image", false);
        OptionData attachment3 = new OptionData(OptionType.ATTACHMENT, "image3", "choose your image", false);
        OptionData attachment4 = new OptionData(OptionType.ATTACHMENT, "image4", "choose your image", false);
        commandData.add(Commands.slash("upload-image", "Upload your image to generate something amazing!")
                .addOptions(attachment, attachment2, attachment3, attachment4));
        commandData.add(Commands.slash("get-images", "Get your currently uploaded images."));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
