package com.github.novicezk.midjourney.bot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratedPromptData {
    private String prompt;

    private String style;

    private String character;

    private CharacterStrength characterStrength;
}
