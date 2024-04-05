package com.github.novicezk.midjourney.bot.utils;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SeasonTracker {
    private static final String FILE_PATH = "generations_count.ser";
    private static final String SEASON_VERSION_KEY = "SEASON_VERSION";

    private static Dotenv config = Dotenv.configure().ignoreIfMissing().load();
    private static Map<Integer, Integer> generationCounts = new HashMap<>();

    static {
        loadGenerationsCount();
    }

    public static int getCurrentSeasonVersion() {
        return Integer.parseInt(config.get(SEASON_VERSION_KEY));
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
