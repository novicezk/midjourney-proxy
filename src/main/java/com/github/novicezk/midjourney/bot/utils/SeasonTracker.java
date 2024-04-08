package com.github.novicezk.midjourney.bot.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SeasonTracker {
    private static final String FILE_PATH = "generations_count.ser";

    private static Map<Integer, Integer> generationCounts = new HashMap<>();

    static {
        loadGenerationsCount();
    }

    public static int getCurrentSeasonVersion() {
        return Config.getSeasonVersion();
    }

    public static int getCurrentGenerationCount() {
        int seasonVersion = getCurrentSeasonVersion();
        return generationCounts.getOrDefault(seasonVersion, 1);
    }

    public static void incrementGenerationCount() {
        int seasonVersion = getCurrentSeasonVersion();
        int currentGenerationCount = generationCounts.getOrDefault(seasonVersion, 1);
        generationCounts.put(seasonVersion, currentGenerationCount + 1);

        saveGenerationsCount();
    }

    private static void saveGenerationsCount() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            outputStream.writeObject(generationCounts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadGenerationsCount() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
                generationCounts = (Map<Integer, Integer>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
