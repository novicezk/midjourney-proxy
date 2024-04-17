package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.images.ImageStorage;
import com.github.novicezk.midjourney.bot.images.ImageValidator;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class CommandsUtil {
    public static List<String> getUserUrls(String userId) {
        List<String> imageUrls = new ArrayList<>();
        for (String url : ImageStorage.getImageUrls(userId)) {
            if (ImageValidator.isValidImageUrl(url)) {
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    public static String generateTitle(boolean isImagesEmpty, String defaultTitle) {
        if (isImagesEmpty) {
            return "Oops! No image uploaded or link expired. We'll use your avatar instead. To upload a new image, try `/upload-image`.\n\n";
        } else {
            return defaultTitle;
        }
    }

    public static String getImageUrlFromDiscordAvatar(User user) {
        String url = null;

        if (user.getAvatarUrl() != null) {
            url = user.getAvatarUrl().replace(".gif", ".png");
        }

        return url;
    }
}
