package com.github.novicezk.midjourney.bot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String SEASON_VERSION = "SEASON_VERSION";
    private static final String DISCORD_BOT_TOKEN = "DISCORD_BOT_TOKEN";
    private static final String IMGBB_TOKEN = "IMGBB_TOKEN";
    private static final String SENDING_CHANNEL = "SENDING_CHANNEL";
    private static final String QUEUE_CHANNEL = "QUEUE_CHANNEL";
    private static final String GUILD_ID = "GUILD_ID";
    private static final String GODFATHER_ID = "GODFATHER_ID";
    private static final String ADMINS_ROLE_ID = "ADMINS_ROLE_ID";
    private static final String CONTACT_MANAGER_ID = "CONTACT_MANAGER_ID";
    private static final String FAQ_CHANNEL_URL = "FAQ_CHANNEL_URL";
    private static final String QUEUE_LIMIT_PER_PERSON = "QUEUE_LIMIT_PER_PERSON";
    private static final String LOGS_CHANNEL = "LOGS_CHANNEL";
    private static final String FAQ_CHANNEL = "FAQ_CHANNEL";

    private static final String CONFIG_FILE = "adam-ai/config.properties";
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getSeasonVersion() {
        return Integer.parseInt(properties.getProperty(SEASON_VERSION));
    }

    public static String getDiscordBotToken() {
        return properties.getProperty(DISCORD_BOT_TOKEN);
    }

    public static String getImgbbToken() {
        return properties.getProperty(IMGBB_TOKEN);
    }

    public static String getSendingChannel() {
        return properties.getProperty(SENDING_CHANNEL);
    }

    public static String getQueueChannel() {
        return properties.getProperty(QUEUE_CHANNEL);
    }

    public static String getGuildId() {
        return properties.getProperty(GUILD_ID);
    }

    public static String getGodfatherId() {
        return properties.getProperty(GODFATHER_ID);
    }

    public static String getAdminsRoleId() {
        return properties.getProperty(ADMINS_ROLE_ID);
    }

    public static String getContactManagerId() {
        return properties.getProperty(CONTACT_MANAGER_ID);
    }

    public static String getFaqChannelUrl() {
        return properties.getProperty(FAQ_CHANNEL_URL);
    }

    public static int getQueueLimitPerPerson() {
        return Integer.parseInt(properties.getProperty(QUEUE_LIMIT_PER_PERSON, "3"));
    }

    public static String getLogsChannel() {
        return properties.getProperty(LOGS_CHANNEL);
    }

    public static String getFaqChannel() {
        return properties.getProperty(FAQ_CHANNEL);
    }
}
