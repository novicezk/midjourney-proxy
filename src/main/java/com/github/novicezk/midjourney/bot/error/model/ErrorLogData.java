package com.github.novicezk.midjourney.bot.error.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorLogData {
    private String errorMessage;
    private String userId;
}
