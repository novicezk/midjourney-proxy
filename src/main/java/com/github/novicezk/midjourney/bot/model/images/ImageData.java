package com.github.novicezk.midjourney.bot.model.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageData {
    private String id;

    private String title;

    @JsonProperty("url_viewer")
    private String urlViewer;

    private String url;

    @JsonProperty("display_url")
    private String displayUrl;

    private int width;

    private int height;

    private int size;

    private long time;

    private int expiration;

    private ImageInfo image;

    private ImageInfo thumb;

    @JsonProperty("delete_url")
    private String deleteUrl;

    private boolean success;

    private int status;
}
