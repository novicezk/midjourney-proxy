package com.github.novicezk.midjourney.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class WelcomeMessageTracker {
    private static final String DATABASE_URL = "jdbc:sqlite:welcome_messages:v2.db";
    private static Set<String> welcomedUsers;

    static {
        initializeDatabase();
    }

    private static void loadData() {
        welcomedUsers = loadWelcomedUsersFromDatabase();
    }

    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS welcome_messages (user_id TEXT PRIMARY KEY)");

            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static Set<String> loadWelcomedUsersFromDatabase() {
        Set<String> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM welcome_messages");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(resultSet.getString("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static boolean hasBeenWelcomed(String userId) {
        log.debug("hasBeenWelcomed {} - {}. list: {}", welcomedUsers.contains(userId), userId, welcomedUsers);
        return welcomedUsers.contains(userId);
    }

    public static void markAsWelcomed(String userId) {
        log.debug("markAsWelcomed {}", userId);
        welcomedUsers.add(userId);
        saveUserToDatabase(userId);
    }

    private static void saveUserToDatabase(String userId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO welcome_messages (user_id) VALUES (?)")) {
            statement.setString(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
