package com.github.novicezk.midjourney.bot.model.images;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class ImageInfo {
    private String filename;

    private String name;

    private String mime;

    private String extension;

    private String url;
}
