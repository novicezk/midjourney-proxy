package com.github.novicezk.midjourney.bot.events;

import com.github.novicezk.midjourney.bot.events.model.EventData;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EventsStorage {
    private static final String DATABASE_URL = "jdbc:sqlite:statistics.db";

    static {
        initializeDatabase();
    }

    public static void logButtonInteraction(String action, String userId) {
        log.info("/button interaction action: {} userId: {}", action, userId);
        logAction(action, userId);
    }

    public static void logCommandInvocation(String commandName, String userId) {
        log.info("/command interaction command name: {} userId: {}", commandName, userId);
        logAction(commandName, userId);
    }

    public static List<EventData> getStatistics() {
        List<EventData> statistics = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            String sql = "SELECT id, action, user_id, timestamp FROM statistics";
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String action = resultSet.getString("action");
                    String userId = resultSet.getString("user_id");
                    Timestamp timestamp = resultSet.getTimestamp("timestamp");
                    statistics.add(new EventData(id, action, userId, timestamp));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statistics;
    }

    private static void logAction(String action, String userId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "INSERT INTO statistics (action, user_id, timestamp) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, action);
                statement.setString(2, userId);
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS statistics (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "action TEXT NOT NULL," +
                    "user_id TEXT NOT NULL," +
                    "timestamp INTEGER NOT NULL)";
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
