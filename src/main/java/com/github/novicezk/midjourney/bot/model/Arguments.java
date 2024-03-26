package com.github.novicezk.midjourney.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Arguments {
    @JsonProperty("aspect_ratio")
    private List<AspectRatio> aspectRatio;

    @JsonProperty("versions")
    private List<Version> versions;
}
