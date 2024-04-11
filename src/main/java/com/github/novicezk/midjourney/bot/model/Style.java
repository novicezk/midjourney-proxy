package com.github.novicezk.midjourney.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public class Style {
    @JsonProperty("name")
    private String name;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("sref")
    private List<String> sref;

    @Nullable
    @JsonProperty("aspect_ratio")
    private List<AspectRatio> aspectRatio;
}
