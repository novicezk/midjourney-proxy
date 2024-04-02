package com.github.novicezk.midjourney.bot.images;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageValidator {
    public static boolean isValidImageUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            // Checking the response status. If the status is 200, then the image is available
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
}
