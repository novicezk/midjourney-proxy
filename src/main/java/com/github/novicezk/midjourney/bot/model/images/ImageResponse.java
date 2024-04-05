package com.github.novicezk.midjourney.bot.model.images;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponse {
    private ImageData data;
    private boolean success;
    private int status;
}
