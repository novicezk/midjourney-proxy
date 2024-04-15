package com.github.novicezk.midjourney.bot.error;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ErrorMessageStorage {
    private static final String DATABASE_URL = "jdbc:sqlite:error_messages.db";

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS error_messages (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT, fail_reason TEXT)"
             )) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveErrorMessage(String userId, String failReason) {
        String sql = "INSERT INTO error_messages (user_id, fail_reason) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, failReason);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getErrorMessages(String userId) {
        List<String> errorMessages = new ArrayList<>();
        String sql = "SELECT fail_reason FROM error_messages WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                errorMessages.add(resultSet.getString("fail_reason"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errorMessages;
    }

    public static List<String> getErrorMessages() {
        List<String> errorMessages = new ArrayList<>();
        String sql = "SELECT id, user_id, fail_reason FROM error_messages";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String userId = resultSet.getString("user_id");
                String failReason = resultSet.getString("fail_reason");
                errorMessages.add("ID: " + id + ", User ID: " + userId + ", Fail Reason: " + failReason);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errorMessages;
    }
}
