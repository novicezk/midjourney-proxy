package com.github.novicezk.midjourney.bot.error;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ErrorMessageStorage {
    private static final String DATABASE_URL = "jdbc:sqlite:error_messages:v2.db";

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS error_messages (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT, fail_reason TEXT, timestamp DATETIME)"
             )) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveErrorMessage(String userId, String failReason) {
        String sql = "INSERT INTO error_messages (user_id, fail_reason, timestamp) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, failReason);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getErrorMessages(String userId) {
        List<String> errorMessages = new ArrayList<>();
        String sql = "SELECT fail_reason, timestamp FROM error_messages WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String failReason = resultSet.getString("fail_reason");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                errorMessages.add("Fail Reason: " + failReason + " Timestamp: " + timestamp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errorMessages;
    }

    public static List<String> getErrorMessages() {
        List<String> errorMessages = new ArrayList<>();
        String sql = "SELECT id, user_id, fail_reason, timestamp FROM error_messages";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String userId = resultSet.getString("user_id");
                String failReason = resultSet.getString("fail_reason");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                errorMessages.add("ID: " + id + ", User ID: " + userId + ", Fail Reason: " + failReason + " Timestamp: " + timestamp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errorMessages;
    }
}
