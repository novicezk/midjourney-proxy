package com.github.novicezk.midjourney.bot.images;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageStorage {
    private static final String DATABASE_URL = "jdbc:sqlite:image_urls.db";

    static {
        initializeDatabase();
    }

    public static void addImageUrl(String userId, List<String> urls) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO image_urls (user_id, url) VALUES (?, ?)")) {
            for (String url : urls) {
                statement.setString(1, userId);
                statement.setString(2, url);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getImageUrls(String userId) {
        List<String> urls = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("SELECT url FROM image_urls WHERE user_id = ?")) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                urls.add(resultSet.getString("url"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return urls;
    }

    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS image_urls (user_id TEXT, url TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
