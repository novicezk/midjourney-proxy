package com.github.novicezk.midjourney.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PromptData {
    @JsonProperty("base_prompt")
    private String basePrompt;

    @JsonProperty("characters")
    private List<Character> characters;

    @JsonProperty("styles")
    private List<Style> styles;

    @JsonProperty("refs")
    private List<Reference> references;

    @JsonProperty("arguments")
    private Arguments arguments;
}
