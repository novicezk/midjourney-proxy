package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

@Slf4j
public class ContractCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "contract";

    private final SubmitController submitController;

    public ContractCommandHandler(SubmitController submitController) {
        this.submitController = submitController;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !isAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping promptMapping = event.getOption("prompt");
        OptionMapping taskMapping = event.getOption("task");
        if (promptMapping != null && !promptMapping.getAsString().isEmpty()) {
            handlePrompt(event, promptMapping.getAsString());
        } else if (taskMapping != null && !taskMapping.getAsString().isEmpty()) {
            handleTask(event, taskMapping.getAsString());
        } else {
            OnErrorAction.onMissingFieldMessage(event);
        }
    }

    private boolean isAuthorized(Member member) {
        String adminsRoleId = Config.getAdminsRoleId();
        String godfatherId = Config.getGodfatherId();
        return member.getRoles().stream()
                .anyMatch(role -> role.getId().equals(adminsRoleId) || role.getId().equals(godfatherId));
    }

    private void handlePrompt(SlashCommandInteractionEvent event, String prompt) {
        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(prompt);
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            CommandsUtil.handleCommandResponse(result, "`" + prompt + "`", prompt, event);
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private void handleTask(SlashCommandInteractionEvent event, String task) {
        if ("test".equals(task)) {
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("test command"))).queue();
        } else if ("faq".equals(task) && event.getGuild() != null) {
            handleFaqCommand(event);
            event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("done")).queue();
        } else {
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("Command not found"))).queue();
        }
    }

    private void handleFaqCommand(SlashCommandInteractionEvent event) {
        event.getGuild().getTextChannelById(Config.getFaqChannel())
                .sendMessageEmbeds(EmbedUtil.createEmbed(
                        "FAQ: Discord Bot Commands",
                        "1. What is the purpose of the \"generate\" command?\n\n" +
                                "**・Command**: generate\n" +
                                "**・Description**: Generate random images of your avatar for inspiration.\n" +
                                "\n" +
                                "2. What does the \"upload-image\" command do?\n\n" +
                                "**・Command**: upload-image\n" +
                                "**・Description**: Upload your images to use with the bot. If none are uploaded or the link expires, your avatar will be used instead.\n" +
                                "\n" +
                                "3. How do I contact artists?\n\n" +
                                "Please contact <@" + Config.getContactManagerId() + ">",
                        null,
                        ColorUtil.getWarningColor()
                )).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}