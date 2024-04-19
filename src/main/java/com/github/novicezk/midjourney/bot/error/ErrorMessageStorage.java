package com.github.novicezk.midjourney.bot.error;

import com.github.novicezk.midjourney.bot.error.model.ErrorLogData;

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

    public static List<ErrorLogData> getErrorMessages(String userId) {
        List<ErrorLogData> errorMessages = new ArrayList<>();
        String sql = "SELECT fail_reason, timestamp FROM error_messages WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String failReason = resultSet.getString("fail_reason");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");

                ErrorLogData data = new ErrorLogData();
                data.setUserId(userId);
                data.setErrorMessage("Fail Reason: " + failReason + " Timestamp: " + timestamp);
                errorMessages.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errorMessages;
    }

    public static List<ErrorLogData> getErrorMessages() {
        List<ErrorLogData> errorMessages = new ArrayList<>();
        String sql = "SELECT id, user_id, fail_reason, timestamp FROM error_messages";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String userId = resultSet.getString("user_id");
                String failReason = resultSet.getString("fail_reason");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");

                ErrorLogData data = new ErrorLogData();
                data.setErrorMessage("ID: " + id + ", User ID: " + userId + ", Fail Reason: " + failReason + " Timestamp: " + timestamp);
                data.setUserId(userId);
                errorMessages.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errorMessages;
    }
}
