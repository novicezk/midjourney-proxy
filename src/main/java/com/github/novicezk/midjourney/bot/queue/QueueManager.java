package com.github.novicezk.midjourney.bot.queue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueueManager {
    private static final String DATABASE_URL = "jdbc:sqlite:user_queue_v2.db";

    static {
        createTableIfNotExists();
    }

    public static void addToQueue(String userId, String taskId, String message) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO user_queue (user_id, task_id, message) VALUES (?, ?, ?)"
             )) {
            statement.setString(1, userId);
            statement.setString(2, taskId);
            statement.setString(3, message);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static QueueEntry removeFromQueue(String userId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM user_queue WHERE user_id = ?"
             )) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                QueueEntry entry = new QueueEntry(
                        resultSet.getString("user_id"),
                        resultSet.getString("task_id"),
                        resultSet.getString("message")
                );
                deleteFromQueue(userId);
                return entry;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void deleteFromQueue(String userId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM user_queue WHERE user_id = ?"
             )) {
            statement.setString(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<QueueEntry> getCurrentQueue() {
        List<QueueEntry> queue = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM user_queue")) {
            while (resultSet.next()) {
                QueueEntry entry = new QueueEntry(
                        resultSet.getString("user_id"),
                        resultSet.getString("task_id"),
                        resultSet.getString("message")
                );
                queue.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return queue;
    }

    public static void clearQueue() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM user_queue");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_queue (user_id TEXT, task_id TEXT PRIMARY KEY, message TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
