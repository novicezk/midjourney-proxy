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
        return createEmbed(null, description, null, Color.white);
    }

    public static MessageEmbed createEmbedWarning(String description) {
        return createEmbed(null, description, null, Color.decode("#EF934D"));
    }

    public static MessageEmbed createEmbedError(String description) {
        return createEmbed(null, description, null, Color.decode("#ED4337"));
    }

    public static MessageEmbed createEmbedWithFooter(String title, String description, String footer) {
        return createEmbed(title, description, footer, Color.white);
    }

    public static MessageEmbed createEmbedWithFooter(String description, String footer) {
        return createEmbed(null, description, footer, Color.white);
    }
}
