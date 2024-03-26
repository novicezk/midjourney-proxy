package com.github.novicezk.midjourney.bot.images;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public class ImageValidator {
    public static boolean isImageAvailable(String imageUrl) {
        try {
            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(imageUrl);
            HttpResponse response = client.execute(request);

            // Checking the response status. If the status is 200, then the image is available
            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
