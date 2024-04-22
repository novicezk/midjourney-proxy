package com.github.novicezk.midjourney.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedUtil {
    public static MessageEmbed createEmbed(String title, String description, String footer, Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(color);

        if (title != null) {
            builder.setTitle(title);
        }

        if (description != null) {
            builder.setDescription(description);
        }

        if (footer != null) {
            builder.setFooter(footer);
        }

        return builder.build();
    }

    public static MessageEmbed createEmbed(String description) {
        return createEmbed(null, description, null, ColorUtil.getDefaultColor());
    }

    public static MessageEmbed createEmbed(String title, String description) {
        return createEmbed(title, description, null, ColorUtil.getDefaultColor());
    }

    public static MessageEmbed createEmbedWarning(String description) {
        return createEmbed(null, description, null, ColorUtil.getWarningColor());
    }

    public static MessageEmbed createEmbedError(String description) {
        return createEmbed(null, description, null, ColorUtil.getErrorColor());
    }

    public static MessageEmbed createEmbedSuccess(String description) {
        return createEmbed(null, description, null, ColorUtil.getSuccessColor());
    }

    public static MessageEmbed createEmbedWithFooter(String title, String description, String footer) {
        return createEmbed(title, description, footer, ColorUtil.getDefaultColor());
    }

    public static MessageEmbed createEmbedWithFooter(String description, String footer) {
        return createEmbed(null, description, footer, ColorUtil.getDefaultColor());
    }
}
