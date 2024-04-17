package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Slf4j
public class ContractCommandHandler {
    private final SubmitController submitController;

    public ContractCommandHandler(SubmitController submitController) {
        this.submitController = submitController;
    }

    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member != null && member.getRoles().stream().anyMatch(role ->
                role.getId().equals(Config.getAdminsRoleId()) || role.getId().equals(Config.getGodfatherId()))) {

            OptionMapping promptMapping = event.getOption("prompt");
            if (promptMapping != null && !promptMapping.getAsString().isEmpty()) {
                String prompt = promptMapping.getAsString();

                SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
                imagineDTO.setPrompt(prompt);
                SubmitResultVO result = submitController.imagine(imagineDTO);
                if (result != null) {
                    CommandsUtil.handleCommandResponse(result, "`" + prompt + "`", prompt, event);
                } else {
                    OnErrorAction.onImageErrorMessage(event);
                }
            } else {
                OnErrorAction.onMissingFieldMessage(event);
            }
        } else {
            OnErrorAction.onMissingRoleMessage(event);
        }
    }
}
