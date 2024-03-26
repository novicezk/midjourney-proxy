package com.github.novicezk.midjourney.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Version {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;
}
