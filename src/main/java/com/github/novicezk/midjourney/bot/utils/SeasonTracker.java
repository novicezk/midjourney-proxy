package com.github.novicezk.midjourney.bot.utils;

 import java.sql.*;

public class SeasonTracker {
    private static final String DATABASE_URL = "jdbc:sqlite:generations_count.db";

    static {
        initializeDatabase();
    }

    public static int getCurrentSeasonVersion() {
        return Config.getSeasonVersion();
    }

    public static int getCurrentGenerationCount() {
        int seasonVersion = getCurrentSeasonVersion();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("SELECT generation_count FROM generations WHERE season_version = ?")) {
            statement.setInt(1, seasonVersion);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("generation_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default value if no data is found
    }

    public static void incrementGenerationCount() {
        int seasonVersion = getCurrentSeasonVersion();
        int currentGenerationCount = getCurrentGenerationCount();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO generations (season_version, generation_count) VALUES (?, ?)")) {
            statement.setInt(1, seasonVersion);
            statement.setInt(2, currentGenerationCount + 1);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS generations (" +
                    "season_version INTEGER PRIMARY KEY," +
                    "generation_count INTEGER NOT NULL DEFAULT 1)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
