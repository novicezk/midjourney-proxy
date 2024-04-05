package com.github.novicezk.midjourney.bot.images;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageStorage {
    private static final String FILE_PATH = "image_urls.ser";
    private static Map<String, List<String>> imageUrlsMap = new HashMap<>();

    static {
        loadImageUrls();
    }

    public static void addImageUrl(String userId, List<String> urls) {
        imageUrlsMap.put(userId, urls);
        saveImageUrls();
    }

    public static List<String> getImageUrls(String userId) {
        return imageUrlsMap.getOrDefault(userId, new ArrayList<>());
    }

    private static void saveImageUrls() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            outputStream.writeObject(imageUrlsMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadImageUrls() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
                imageUrlsMap = (Map<String, List<String>>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
